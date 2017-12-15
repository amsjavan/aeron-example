package ir.amsjavan.bench

import ir.amsjavan.client.ClientAeron
import ir.amsjavan.server.AeronServer

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

object Benchmark extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  val iteration = 100000
  val server = AeronServer
  Thread.sleep(2000)
  val client = ClientAeron

  val result = Future {
    client.test(iteration)
  }
  Thread.sleep(2000)

  Future {

    server.test(iteration)

  }

  Await.result(result, Duration.Inf)

}
