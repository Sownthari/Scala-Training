name := "producer-service"

version := "0.1.0"

scalaVersion := "2.13.13"

lazy val akkaVersion = "2.6.20"
lazy val akkaHttpVersion = "10.2.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,            // CLASSIC
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "org.apache.kafka"  %  "kafka-clients" % "3.5.0",
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "ch.qos.logback"    %  "logback-classic" % "1.4.11"
)

fork := true
