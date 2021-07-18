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
  .aggregate(`prime-number-server`, `proxy-server-api`, `proxy-server-impl`)

lazy val `prime-number-server-HTTP-port` = 11000

lazy val `prime-number-server` = (project in file("prime-number-server"))
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
    lagomServiceHttpPort := `prime-number-server-HTTP-port`,

    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      playGrpcRuntime,
      scalaTest,
      lagomGrpcTestkit
    )
  ).settings(lagomForkedTestSettings: _*)
  .settings(dockerSettings)

lazy val `proxy-server-api` = (project in file("proxy-server-api"))
  .settings(
    libraryDependencies += lagomScaladslApi
  )

lazy val `proxy-server-impl` = (project in file("proxy-server-impl"))
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
    scalaTest
  ),

  // WORKAROUND: for akka discovery method lookup in dev-mode
  lagomDevSettings := Seq("akka.discovery.method" -> "lagom-dev-mode")
)
  .settings(dockerSettings)
  .dependsOn(`proxy-server-api`)

ThisBuild / lagomCassandraEnabled := false
ThisBuild / lagomKafkaEnabled := false

// This adds an entry on the LagomDevMode Service Registry. With this information on the Service Registry a client
// using Service Discovery to Lookup("helloworld.GreeterService") will get "http://localhost:11000" and then be able to send a request.
ThisBuild / lagomUnmanagedServices := Map("helloworld.GreeterService" -> s"http://localhost:${`prime-number-server-HTTP-port`}")
