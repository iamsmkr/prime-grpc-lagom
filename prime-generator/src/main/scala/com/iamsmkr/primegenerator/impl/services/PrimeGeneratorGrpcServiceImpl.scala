package com.iamsmkr.primegenerator.impl.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.iamsmkr.primegenerator.grpc._

class PrimeGeneratorGrpcServiceImpl(system: ActorSystem) extends AbstractPrimeGeneratorServiceRouter(system) {

  override def getPrimeNumbers(req: GetPrimeNumbersRequest): Source[GetPrimeNumbersReply, NotUsed] = {

    def sieve(s: Stream[Int]): Stream[Int] = s.head #:: sieve(s.tail.filter(_ % s.head != 0))

    Source(sieve(Stream.from(2)))
      .takeWhile(_ <= req.number)
      .map(n => GetPrimeNumbersReply(n))

  }
}
