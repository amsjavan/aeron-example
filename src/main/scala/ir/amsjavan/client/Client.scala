package ir.amsjavan.client

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import io.aeron.Aeron
import io.aeron.logbuffer.{ FragmentHandler, Header }
import org.agrona.DirectBuffer
import org.agrona.concurrent.BackoffIdleStrategy

object Client extends App {
  ClientAeron.run
}

object ClientAeron {
  println("Initializing Client")

  val aeron = Aeron.connect(new Aeron.Context())
  val subscription = aeron.addSubscription("aeron:udp?endpoint=localhost:40123", 10)

  val idleStrategy = new BackoffIdleStrategy(
    100, 10, TimeUnit.MICROSECONDS.toNanos(1), TimeUnit.MICROSECONDS.toNanos(100))
  val handler = new FragmentHandler {
    override def onFragment(buffer: DirectBuffer, offset: Int, length: Int, header: Header): Unit = {
      val data = Array.ofDim[Byte](length)
      buffer.getBytes(offset, data)

      new String(data) match {
        case "exit" => System.exit(0)
        case output => println(s"message to stream ${header.streamId()} from session ${header.sessionId()} ($length $offset) <<${output}>>")
      }
    }
  }

  var counter = 0
  var start: Long = 0
  class TestHandler(iteration: Int) extends FragmentHandler {
    override def onFragment(buffer: DirectBuffer, offset: Int, length: Int, header: Header): Unit = {
      val data = Array.ofDim[Byte](length)
      buffer.getBytes(offset, data)
      counter += 1

      if (counter == 1) {
        start = System.currentTimeMillis()
      }
      if (counter == iteration) {
        println(s"$counter messages processed in ${System.currentTimeMillis() - start} ms")
      }
    }
  }
  def run: Unit = {
    while (true) {
      val fragmentsRead = subscription.poll(handler, 10)
      idleStrategy.idle(fragmentsRead)
    }
  }

  def test(iteration: Int): Unit = {
    while (counter < iteration) {
      val fragmentsRead = subscription.poll(new TestHandler(iteration), 10)
      idleStrategy.idle(fragmentsRead)
    }
  }
}