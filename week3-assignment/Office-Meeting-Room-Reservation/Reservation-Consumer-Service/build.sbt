name := "reservation-consumer-service"

version := "0.1.0"

scalaVersion := "2.13.13"

lazy val akkaVersion = "2.6.20"
lazy val akkaHttpVersion = "10.2.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "org.apache.kafka" %% "kafka" % "3.7.0" ,// Kafka client
  "org.apache.kafka"  %  "kafka-clients" % "3.5.0",
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "ch.qos.logback"    %  "logback-classic" % "1.4.11",
  "com.sun.mail" % "jakarta.mail" % "2.0.2"
)

fork := true