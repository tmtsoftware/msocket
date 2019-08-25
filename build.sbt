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
    `akka-js`,
    `msocket`,
    `simple-service`
  )

//************* akka-api *****************************************************

lazy val `akka-js` = project.enablePlugins(ScalaJSPlugin)

//************* msocket *****************************************************

lazy val `msocket` = project.aggregate(
  `msocket-api`.jvm,
  `msocket-api`.js,
  `msocket-impl-jvm`,
  `msocket-impl-js`
)

lazy val `msocket-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("msocket/msocket-api"))
  .jsConfigure(_.dependsOn(`akka-js`))
  .jvmSettings(
    libraryDependencies ++= Seq(
      `akka-stream`
    )
  )
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`,
      `borer-derivation`
    )
  )

lazy val `msocket-impl-jvm` = project
  .in(file("msocket/msocket-impl-jvm"))
  .dependsOn(`msocket-api`.jvm)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http`,
      `borer-compat-akka`
    )
  )

lazy val `msocket-impl-js` = project
  .in(file("msocket/msocket-impl-js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`msocket-api`.js)
  .settings(
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    libraryDependencies ++= Seq(
      `scalajs-dom`.value
    )
  )

//************* simple-service *****************************************************

lazy val `simple-service` = project.aggregate(
  `simple-service-api`.jvm,
  `simple-service-api`.js,
  `simple-service-impl`,
  `simple-service-server`,
  `simple-service-app-jvm`
)

lazy val `simple-service-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("simple-service/simple-service-api"))
  .dependsOn(`msocket-api`)

lazy val `simple-service-impl` = project
  .in(file("simple-service/simple-service-impl"))
  .dependsOn(`simple-service-api`.jvm)

lazy val `simple-service-server` = project
  .in(file("simple-service/simple-service-server"))
  .dependsOn(`simple-service-impl`, `msocket-impl-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `scalatest`.value     % Test,
      `akka-http-testkit`   % Test,
      `akka-stream-testkit` % Test
    )
  )

lazy val `simple-service-app-jvm` = project
  .in(file("simple-service/simple-service-app-jvm"))
  .dependsOn(`simple-service-api`.jvm, `msocket-impl-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `scalatest`.value % Test
    )
  )

lazy val `simple-service-app-js` = project
  .in(file("simple-service/simple-service-app-js"))
  .dependsOn(`simple-service-api`.js, `msocket-impl-js`)
  .settings(
    libraryDependencies ++= Seq(
      `scalatest`.value % Test
    )
  )
