ThisBuild / organization := "org.vspaz"
ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.2"

val scalaDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.10.2",
  "org.apache.logging.log4j" % "log4j-api" % "2.24.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.24.3",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.24.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.18.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.2",

  // Test dependencies
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.junit.jupiter" % "junit-jupiter-api" % "5.11.4",
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
