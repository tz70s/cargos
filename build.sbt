name := "cargos"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6"
)

lazy val libraries = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-http"   % "10.1.3",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.13",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.4.0",
  "com.github.pureconfig" %% "pureconfig" % "0.9.1"
)

lazy val cargos = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= libraries
  )

dockerBaseImage := "openjdk:jre"
maintainer := "Tzu-Chiao Yeh <su3g4284zo6y7@gmail.com>"
packageSummary := "Microservice for cargo recognition service."
packageName := "cargos"
dockerExposedPorts := Seq(8080)
dockerUsername := Some("tz70s")