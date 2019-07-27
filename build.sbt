import Dependencies._
import Borer._

inThisBuild(
  Seq(
    scalaVersion := "2.12.8",
    version := "0.1.0-SNAPSHOT",
    organization := "com.github.mushtaq.msocket",
    organizationName := "ThoughtWorks",
    resolvers += Resolver.jcenterRepo,
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xlint:_,-missing-interpolator",
      "-Ywarn-dead-code"
      //      "-Xprint:typer"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin(`silencer-plugin`),
      `silencer-lib` % Provided
    )
  )
)

lazy val `root` = project
  .in(file("."))
  .aggregate(
    `msocket-core`,
    `msocket-simple-example`,
    `simple-service`
  )

lazy val `msocket-core` = project
  .settings(
    libraryDependencies ++= Seq(
      `akka-stream`,
      `akka-http`,
      `borer-core`,
      `borer-derivation`,
      `borer-compat-akka`
    )
  )

lazy val `msocket-simple-example` = project
  .dependsOn(`simple-service`, `msocket-core`)

lazy val `simple-service` = project
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`,
      `borer-derivation`,
      `akka-stream`
    )
  )
