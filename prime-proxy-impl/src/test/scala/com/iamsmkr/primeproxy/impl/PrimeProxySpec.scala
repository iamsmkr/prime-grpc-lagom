package com.iamsmkr.primeproxy.impl

import akka._
import akka.actor.ActorSystem
import akka.discovery._
import akka.discovery.ServiceDiscovery._
import akka.grpc.scaladsl.AkkaGrpcClient
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.iamsmkr.primegenerator.grpc._
import com.iamsmkr.primeproxy.api.PrimeProxyService
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration.FiniteDuration

class PrimeProxySpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[PrimeProxyApplication] =
    ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
      new PrimeProxyApplication(ctx)
        with LocalServiceLocator
        with ProvidesAdditionalConfiguration {

        // Uses Lagom's `ProvidesAdditionalConfiguration` cake layer to setup
        // a test-friendly "akka.discovery.method" (see below)
        override def additionalConfiguration: AdditionalConfiguration =
          super.additionalConfiguration ++ ConfigFactory.parseString(
            s"akka.discovery.method = ${classOf[PlaceholderServiceDiscovery].getName}"
          )

        override lazy val primeGeneratorGrpcServiceClient: PrimeGeneratorServiceClient = new PrimeGeneratorServiceClientStub
      }
    }

  implicit val mat: Materializer = server.materializer

  val client: PrimeProxyService = server.serviceClient.implement[PrimeProxyService]

  override protected def afterAll(): Unit = {
    server.stop()
  }

  "PrimeProxy service" should {
    "should return list of prime numbers up until a given prime number queried from PrimeGenerator service over gRPC call" in {
      client.getPrimeNumbers(23L).invoke().map { answer =>
        answer should be("2,3,5,7,11,13,17,19,23")
      }
    }
  }
}

protected trait StubbedAkkaGrpcClient extends AkkaGrpcClient {
  private val _closed = Promise[Done]()

  override def close(): Future[Done] = {
    _closed.trySuccess(Done)
    _closed.future
  }

  override def closed(): Future[Done] = _closed.future
}


class PrimeGeneratorServiceClientStub extends PrimeGeneratorServiceClient with StubbedAkkaGrpcClient {
  override def getPrimeNumbers(in: GetPrimeNumbersRequest): Source[GetPrimeNumbersReply, NotUsed] =
    Source(List(2, 3, 5, 7, 11, 13, 17, 19, 23)).map(n => GetPrimeNumbersReply(n))
}

// At the moment, gRPC client obtains a `SimpleServiceDiscovery` from the ActorSystem default settings
// but this test doesn't exercise that `SimpleServiceDiscovery` instance so we use a noop placeholder.
class PlaceholderServiceDiscovery(system: ActorSystem) extends ServiceDiscovery {
  implicit val exCtx: ExecutionContext = system.dispatcher

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[Resolved] = Future {
    Resolved(lookup.serviceName, immutable.Seq(ResolvedTarget("localhost", None, None)))
  }
}
