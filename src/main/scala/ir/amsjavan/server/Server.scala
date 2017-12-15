package ir.amsjavan.server

import java.nio.ByteBuffer

import io.aeron.Aeron
import io.aeron.driver.MediaDriver
import org.agrona.concurrent.UnsafeBuffer

object Server extends App {

  AeronServer.run

}

object AeronServer {
  val driver = MediaDriver.launch()
  val aeron = Aeron.connect(new Aeron.Context())
  val publication = aeron.addPublication("aeron:udp?endpoint=localhost:40123", 10)
  var BUFFER = new UnsafeBuffer(ByteBuffer.allocateDirect(256))
  def run: Unit = {

    while (true) {
      val message = scala.io.StdIn.readLine()

      println(message)
      BUFFER.putBytes(0, message.getBytes())

      val resultingPosition = publication.offer(BUFFER, 0, message.getBytes().length)
      println(resultingPosition)
    }

  }
}
