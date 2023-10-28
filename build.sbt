ThisBuild / organization := "org.vspaz"
ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"

val scalaDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.8.16",
  "org.apache.logging.log4j" % "log4j-api" % "2.21.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.21.1",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.21.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.3",

  // Test dependencies
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.junit.jupiter" % "junit-jupiter-api" % "5.10.0",
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
