import Dependencies._
import Borer._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

inThisBuild(
  Seq(
    scalaVersion := "2.13.0",
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
    `akka-api-js`,
    `akka-api-jvm`,
    `msocket-api-js`,
    `msocket-api-jvm`,
    `msocket-impl`,
    `msocket-simple-example`,
    `simple-service`
  )

lazy val `msocket-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(`akka-api`)
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`,
      `borer-derivation`
    )
  )

lazy val `msocket-api-js`  = `msocket-api`.js
lazy val `msocket-api-jvm` = `msocket-api`.jvm

lazy val `msocket-impl` = project
  .dependsOn(`msocket-api-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http`,
      `borer-compat-akka`
    )
  )

lazy val `msocket-simple-example` = project
  .dependsOn(`simple-service`, `msocket-impl`)
  .settings(
    libraryDependencies ++= Seq(
      `scalatest`           % Test,
      `akka-http-testkit`   % Test,
      `akka-stream-testkit` % Test
    )
  )
lazy val `simple-service` = project
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`,
      `borer-derivation`,
      `akka-stream`
    )
  )

lazy val `akka-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Dummy)
  .jvmSettings(libraryDependencies ++= Seq(`akka-stream`))
  .settings(fork := false)

lazy val `akka-api-js`  = `akka-api`.js
lazy val `akka-api-jvm` = `akka-api`.jvm
