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
    `akka-api`.jvm,
    `akka-api`.js,
    `msocket`,
    `simple-service`
  )

//************* akka-api *****************************************************

lazy val `akka-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Dummy)
  .jvmSettings(libraryDependencies ++= Seq(`akka-stream`))

//************* msocket *****************************************************

lazy val `msocket` = project.aggregate(
  `msocket-api`.jvm,
  `msocket-api`.js,
  `msocket-impl`
)

lazy val `msocket-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("msocket/msocket-api"))
  .dependsOn(`akka-api`)
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`,
      `borer-derivation`
    )
  )

lazy val `msocket-impl` = project
  .in(file("msocket/msocket-impl"))
  .dependsOn(`msocket-api`.jvm)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http`,
      `borer-compat-akka`
    )
  )

//************* simple-service *****************************************************

lazy val `simple-service` = project.aggregate(
  `simple-service-api`.jvm,
  `simple-service-api`.js,
  `simple-service-impl`,
  `simple-service-server`
)

lazy val `simple-service-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("simple-service/simple-service-api"))
  .dependsOn(`akka-api`)
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`,
      `borer-derivation`
    )
  )

lazy val `simple-service-impl` = project
  .in(file("simple-service/simple-service-impl"))
  .dependsOn(`simple-service-api`.jvm)

lazy val `simple-service-server` = project
  .in(file("simple-service/simple-service-server"))
  .dependsOn(`simple-service-impl`, `msocket-impl`)
  .settings(
    libraryDependencies ++= Seq(
      `scalatest`           % Test,
      `akka-http-testkit`   % Test,
      `akka-stream-testkit` % Test
    )
  )
