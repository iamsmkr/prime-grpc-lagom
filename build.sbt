import Dependencies._
import play.grpc.gen.scaladsl.{PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator}

ThisBuild / organization := "com.iamsmkr"
ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.14"
ThisBuild / scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked")

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := "adoptopenjdk/openjdk8",
  dockerUsername := sys.props.get("docker.username"),
  dockerRepository := sys.props.get("docker.registry")
)

lazy val `prime-grpc-scala` = (project in file("."))
  .aggregate(`prime-generator`, `prime-proxy-api`, `prime-proxy-impl`)

lazy val `prime-generator-HTTP-port` = 11000

lazy val `prime-generator` = (project in file("prime-generator"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin)
  .enablePlugins(PlayAkkaHttp2Support)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources :=
      Seq(
        AkkaGrpc.Server,

        // client is used only in tests. See https://github.com/akka/akka-grpc/issues/410
        AkkaGrpc.Client
      ),
    akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator,

    // WORKAROUND: lagom still can't register a service under the gRPC name so we hard-code
    // the port and the use the value to add the entry on the Service Registry
    lagomServiceHttpPort := `prime-generator-HTTP-port`,
    lagomServiceAddress := "0.0.0.0",

    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      akkaHttp,
      playGrpcRuntime,
      scalaTest,
      lagomGrpcTestkit
    )
  ).settings(lagomForkedTestSettings: _*)
  .settings(dockerSettings)

lazy val `prime-proxy-api` = (project in file("prime-proxy-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  )

lazy val `prime-proxy-impl` = (project in file("prime-proxy-impl"))
  .enablePlugins(LagomScala)
  .enablePlugins(AkkaGrpcPlugin)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcExtraGenerators += PlayScalaClientCodeGenerator,
  ).settings(
  libraryDependencies ++= Seq(
    lagomScaladslAkkaDiscovery,
    lagomScaladslTestKit,
    playGrpcRuntime,
    macwire,
    akkaHttp,
    scalaTest
  ),

  // WORKAROUND: for akka discovery method lookup in dev-mode
  lagomDevSettings := Seq("akka.discovery.method" -> "lagom-dev-mode"),
  lagomServiceAddress := "0.0.0.0"
)
  .settings(dockerSettings)
  .dependsOn(`prime-proxy-api`)

ThisBuild / lagomCassandraEnabled := false
ThisBuild / lagomKafkaEnabled := false

// This adds an entry on the LagomDevMode Service Registry. With this information on the Service Registry a client
// using Service Discovery to Lookup("prime.PrimeGeneratorService") will get "http://127.0.0.1:11000" and then be able to send a request.
ThisBuild / lagomUnmanagedServices := Map("prime.PrimeGeneratorService" -> s"http://127.0.0.1:${`prime-generator-HTTP-port`}")
