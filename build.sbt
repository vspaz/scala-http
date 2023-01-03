ThisBuild / organization := "org.vspaz"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.17"


val scalaDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.8.6",
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
)

libraryDependencies ++= scalaDependencies

lazy val root = (project in file(".")).settings(
  name := "scala-http",
  assembly / mainClass := Some("org.vspaz.Main"),
)