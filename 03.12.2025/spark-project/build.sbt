ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "spark-project",

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.2.1",
      "org.apache.spark" %% "spark-sql" % "3.2.1",
      "org.scalatest" %% "scalatest" % "3.2.2" % "test",
      "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.1",
      "com.github.jnr" % "jnr-posix" % "3.1.7",
      "joda-time" % "joda-time" % "2.10.10",
      "org.apache.spark" %% "spark-streaming" % "3.2.1",
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.2.1",
      "org.apache.spark" %% "spark-avro" % "3.2.1",
      "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.2.1",
      "org.apache.kafka" %% "kafka" % "3.6.0",
      "mysql" % "mysql-connector-java" % "8.0.19",
      "org.apache.hadoop" % "hadoop-aws" % "3.3.1",
      "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.1026"
    ),
    libraryDependencies += "com.typesafe" % "config" % "1.4.2"

)
