package com.iamsmkr.primeproxy.impl

import com.iamsmkr.primeproxy.api.PrimeProxyService
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}

class PrimeProxyLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new PrimeProxyApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PrimeProxyApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[PrimeProxyService])
}
