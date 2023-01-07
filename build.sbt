ThisBuild / organization := "org.vspaz"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.17"

val scalaDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.8.6",
  "org.apache.logging.log4j" % "log4j-api" % "2.19.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.19.0",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.19.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.1",
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
  "org.junit.jupiter" % "junit-jupiter-api" % "5.9.1",
  "com.novocode" % "junit-interface" % "0.11"
)

libraryDependencies ++= scalaDependencies

lazy val root = (project in file(".")).settings(
  name := "scala-http",
  assembly / mainClass := Some("org.vspaz.Main"),
  ThisBuild / assemblyMergeStrategy := {
    case PathList("META-INF", _*) => MergeStrategy.discard
    case _                        => MergeStrategy.first
  }
)
