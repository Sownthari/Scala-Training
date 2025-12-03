ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "spark-day2",

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.2.1",
      "org.apache.spark" %% "spark-sql"  % "3.2.1",
      "org.apache.hadoop" % "hadoop-common" % "3.3.1",
      "mysql" % "mysql-connector-java" % "8.0.19"
    )
  )