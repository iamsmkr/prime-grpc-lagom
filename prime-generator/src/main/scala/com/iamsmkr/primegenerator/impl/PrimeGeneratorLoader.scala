package com.iamsmkr.primegenerator.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._

class PrimeGeneratorLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new PrimeGeneratorApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PrimeGeneratorApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[PrimeGeneratorService])
}
