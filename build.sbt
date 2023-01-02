ThisBuild / organization := "org.vspaz"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.17"

lazy val root = (project in file("."))
  .settings(
    name := "scala-http",
  )

val scalaDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.8.6",
  "org.scalatest" %% "scalatest" % "3.2.14" % Test
)
