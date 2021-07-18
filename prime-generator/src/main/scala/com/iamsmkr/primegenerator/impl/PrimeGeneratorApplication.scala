package com.iamsmkr.primegenerator.impl

import com.iamsmkr.primegenerator.impl.services.PrimeGeneratorGrpcServiceImpl
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

trait PrimeGeneratorService extends Service {
  override def descriptor: Descriptor = named("prime-generator")
}

class PrimeGeneratorServiceImpl() extends PrimeGeneratorService

abstract class PrimeGeneratorApplication(context: LagomApplicationContext)
  extends LagomApplication(context) with AhcWSComponents {

  override lazy val lagomServer: LagomServer = {
    serverFor[PrimeGeneratorService](wire[PrimeGeneratorServiceImpl])
      .additionalRouter(wire[PrimeGeneratorGrpcServiceImpl])
  }

}
