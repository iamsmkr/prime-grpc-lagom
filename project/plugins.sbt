val lagomVersion = "1.6.5"
val akkaGrpcVersion = "2.1.0-M1"
val playGrpcVersion = "0.9.1"

addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % lagomVersion)
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % akkaGrpcVersion)

libraryDependencies +=  "com.lightbend.play" %% "play-grpc-generators" % playGrpcVersion
