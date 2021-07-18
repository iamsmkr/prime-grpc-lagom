import sbt._

object Dependencies {
  private lazy val macwireVersion = "2.3.3"
  private lazy val scalaTestVersion = "3.1.4"
  private lazy val playGrpcVersion = "0.9.1"
  private lazy val akkaHttpVersion = "10.1.14"

  // -- gRPC --
  val playGrpcRuntime = "com.lightbend.play" %% "play-grpc-runtime" % playGrpcVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion

  // -- Dependency Injection --
  val macwire = "com.softwaremill.macwire" %% "macros" % macwireVersion % Provided

  // -- Testing --
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  val lagomGrpcTestkit = "com.lightbend.play" %% "lagom-scaladsl-grpc-testkit" % playGrpcVersion % Test
}
