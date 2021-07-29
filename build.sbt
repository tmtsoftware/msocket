import Libs._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

inThisBuild(
  Seq(
    scalaVersion := "2.13.6",
    version := "0.1.0-SNAPSHOT",
    organization := "com.github.tmtsoftware.msocket",
    organizationName := "ThoughtWorks",
    scalafmtOnCompile := true,
    resolvers += "jitpack" at "https://jitpack.io",
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xasync",
      "-Wconf:any:warning-verbose",
      "-Wdead-code",
      "-Xlint:_,-missing-interpolator",
      "-Xsource:3",
      "-Xcheckinit"
    )
  )
)

lazy val `msocket-root` = project
  .in(file("."))
  .aggregate(
    `portable`,
    `msocket`,
    `example`
  )

//************* portable-api *****************************************************

lazy val portable = project.aggregate(
  `portable-observer`.jvm,
  `portable-observer`.js,
  `portable-akka`.jvm,
  `portable-akka`.js
)

lazy val `portable-observer` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("portable/portable-observer"))

lazy val `portable-akka` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Dummy)
  .in(file("portable/portable-akka"))
  .dependsOn(`portable-observer`)
  .jvmSettings(
    libraryDependencies ++= Seq(
      `akka-stream`,
      `akka-actor-typed`
    )
  )

//************* msocket *****************************************************

lazy val msocket = project.aggregate(
  `msocket-api`.jvm,
  `msocket-api`.js,
  `msocket-security`,
  `msocket-jvm`,
  `msocket-http`,
  `msocket-rsocket`,
  `msocket-js`
)

lazy val `msocket-security` = project
  .in(file("msocket/msocket-security"))
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`.value,
      `borer-derivation`.value
    )
  )

lazy val `msocket-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("msocket/msocket-api"))
  .dependsOn(`portable-akka`)
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`.value,
      `borer-derivation`.value
    )
  )

lazy val `msocket-jvm` = project
  .in(file("msocket/msocket-jvm"))
  .dependsOn(`msocket-api`.jvm, `msocket-security`)
  .settings(
    libraryDependencies ++= Seq(
      Prometheus.simpleclient,
      Prometheus.simpleclient_common,
      scalatest.value % Test
    )
  )

lazy val `msocket-http` = project
  .in(file("msocket/msocket-http"))
  .dependsOn(`msocket-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http`,
      `borer-compat-akka`,
      scalatest.value            % Test,
      `akka-actor-testkit-typed` % Test,
      `akka-http-testkit`        % Test
    )
  )

lazy val `msocket-rsocket` = project
  .in(file("msocket/msocket-rsocket"))
  .dependsOn(`msocket-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `rsocket-transport-netty`,
      `rsocket-core`
    )
  )

lazy val `msocket-js` = project
  .in(file("msocket/msocket-js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`msocket-api`.js)
  .settings(
    libraryDependencies ++= Seq(
      `tmt-typed`.value,
      `scalajs-dom`.value
    )
  )

//************* example-service *****************************************************

lazy val `example` = project.aggregate(
  `example-service-api`.jvm,
  `example-service-api`.js,
  `example-service`,
  `example-server`,
  `example-client-jvm`,
  `example-client-js`,
  `example-client-jvm-test`
)

lazy val `example-service-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("example/example-service-api"))
  .dependsOn(`msocket-api`)

lazy val `example-service` = project
  .in(file("example/example-service"))
  .dependsOn(`example-service-api`.jvm, `msocket-jvm`)

lazy val `example-server` = project
  .in(file("example/example-server"))
  .dependsOn(`example-service`, `msocket-http`, `msocket-rsocket`)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http-cors`,
      scalatest.value       % Test,
      `akka-http-testkit`   % Test,
      `akka-stream-testkit` % Test
    )
  )

lazy val `example-client-jvm` = project
  .in(file("example/example-client-jvm"))
  .dependsOn(`example-service-api`.jvm, `msocket-http`, `msocket-rsocket`)

lazy val `example-client-jvm-test` = project
  .in(file("example/example-client-jvm-test"))
  .dependsOn(`example-server`, `example-client-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `akka-stream-testkit` % Test,
      scalatest.value       % Test
    )
  )

lazy val `example-client-js` = project
  .in(file("example/example-client-js"))
  .enablePlugins(ScalaJSPlugin, SnowpackPlugin)
  .dependsOn(`example-service-api`.js, `msocket-js`)
  .settings(
    libraryDependencies ++= Seq(
      scalatest.value % Test,
      `scala-async`
    ),
    Test / test := {
//      (`example-server` / reStart).toTask("").value
//      (Test / reStartSnowpackServer).value
      (Test / test).value
//      (Test / testHtml).value
    },
    Compile / run := {
//      (`example-server` / reStart).toTask("").value
//      (Compile / reStartSnowpackServer).value
      (Compile / run).toTask("").value
    }
  )
