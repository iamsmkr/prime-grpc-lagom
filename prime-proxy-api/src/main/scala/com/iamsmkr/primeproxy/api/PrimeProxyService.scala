package com.iamsmkr.primeproxy.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait PrimeProxyService extends Service {

  def getPrimeNumbers(n: Long): ServiceCall[NotUsed, String]

  override final def descriptor: Descriptor = {
    import Service._

    named("prime-proxy")
      .withCalls(
        restCall(Method.GET, "/prime/:number", getPrimeNumbers _)
      ).withAutoAcl(true)
  }
}
