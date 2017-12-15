package ir.amsjavan.client

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import io.aeron.Aeron
import io.aeron.logbuffer.{FragmentHandler, Header}
import org.agrona.DirectBuffer
import org.agrona.concurrent.BackoffIdleStrategy


object Client  extends App{
  ClientAeron.run
}


object ClientAeron {
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
  def run: Unit = {
    while (true) {
      val fragmentsRead = subscription.poll(handler, 10)
      idleStrategy.idle(fragmentsRead)
    }
  }
}