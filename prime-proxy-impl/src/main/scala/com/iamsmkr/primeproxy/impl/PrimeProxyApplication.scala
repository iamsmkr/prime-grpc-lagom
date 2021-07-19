package com.iamsmkr.primeproxy.impl

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.grpc.GrpcClientSettings
import com.iamsmkr.primegenerator.grpc.PrimeGeneratorServiceClient
import com.iamsmkr.primeproxy.api.PrimeProxyService
import com.iamsmkr.primeproxy.impl.services.PrimeProxyServiceImpl
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext}
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContextExecutor

abstract class PrimeProxyApplication(context: LagomApplicationContext)
  extends LagomApplication(context) with AhcWSComponents {

  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val sys: ActorSystem = actorSystem

  private lazy val settings = GrpcClientSettings
    .usingServiceDiscovery("prime-generator")
    .withServicePortName("http")
    .withServiceProtocol("tcp")
    .withTls(false)
    .withConnectionAttempts(5)

  lazy val primeGeneratorGrpcServiceClient = PrimeGeneratorServiceClient(settings)
  //  register a shutdown task to release resources of the client
  coordinatedShutdown
    .addTask(
      CoordinatedShutdown.PhaseServiceUnbind,
      "shutdown-prime-generator-grpc-client"
    ) { () => primeGeneratorGrpcServiceClient.close() }

  override lazy val lagomServer = serverFor[PrimeProxyService](wire[PrimeProxyServiceImpl])
}
