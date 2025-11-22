name := """meeting-room-system"""
organization := "meeting-room-system"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.17"

libraryDependencies += guice
libraryDependencies += "org.playframework" %% "play-guice" % "3.0.9"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies ++= Seq(
  "org.playframework" %% "play-slick" % "6.1.0",
  "org.playframework" %% "play-slick-evolutions" % "6.1.0",
  "mysql" % "mysql-connector-java" % "8.0.26"
)

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-stream" % "1.0.1",
  "com.auth0" % "java-jwt" % "4.3.0",
  "com.typesafe.play" %% "play-json" % "2.9.4"
)
libraryDependencies += "org.apache.pekko" %% "pekko-actor" % "1.0.1"

libraryDependencies += ws
libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0"


libraryDependencies += filters




