package com.iamsmkr.primegenerator.impl

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Sink
import com.iamsmkr.primegenerator.grpc.{GetPrimeNumbersRequest, PrimeGeneratorServiceClient}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{BeforeAndAfterAll, Ignore}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@Ignore
class PrimeGeneratorSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[PrimeGeneratorApplication with LocalServiceLocator] =
    ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
      new PrimeGeneratorApplication(ctx) with LocalServiceLocator
    }

  implicit val sys: ActorSystem = server.actorSystem

  val client: PrimeGeneratorServiceClient =
    PrimeGeneratorServiceClient(
      GrpcClientSettings
        .connectToServiceAt("127.0.0.1", server.playServer.httpPort.get)(server.actorSystem)
        .withTls(false)
    )

  override protected def afterAll(): Unit = {
    client.close()
    server.stop()
  }

  "Prime Generator service" should {

    "reply with a comma-separated list of prime numbers up until a given prime number" in {
      Await.result(client
        .getPrimeNumbers(GetPrimeNumbersRequest(23))
        .map(_.primeNumber)
        .runWith(Sink.seq)
        .map(_.mkString(",")), 5.seconds) should be("2,3,5,7,11,13,17,19,23")
    }
  }
}
