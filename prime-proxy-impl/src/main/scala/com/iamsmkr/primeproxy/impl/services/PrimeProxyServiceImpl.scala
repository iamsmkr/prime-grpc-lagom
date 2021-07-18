package com.iamsmkr.primeproxy.impl.services

import akka.actor.ActorSystem
import akka.NotUsed
import com.iamsmkr.primegenerator.grpc._
import com.iamsmkr.primeproxy.api.PrimeProxyService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import akka.stream.scaladsl.Sink

import scala.concurrent._

class PrimeProxyServiceImpl(primeGeneratorGrpcServiceClient: PrimeGeneratorServiceClient)
                           (implicit exCtx: ExecutionContext, sys: ActorSystem) extends PrimeProxyService {
  val MAX_ALLOWED_SIZE = 10000

  override def getPrimeNumbers(n: Long): ServiceCall[NotUsed, String] = ServiceCall { _ =>

    primeGeneratorGrpcServiceClient
      .getPrimeNumbers(GetPrimeNumbersRequest(n))
      .map(_.primeNumber)
      .take(MAX_ALLOWED_SIZE)
      .runWith(Sink.seq)
      .map(_.mkString(","))
  }
}
