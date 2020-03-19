import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import Libs._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

inThisBuild(
  Seq(
    scalaVersion := "2.13.1",
    version := "0.1.0-SNAPSHOT",
    organization := "com.github.tmtsoftware.msocket",
    organizationName := "ThoughtWorks",
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      "jitpack" at "https://jitpack.io",
      Resolver.bintrayRepo("lonelyplanet", "maven"),
      Resolver.bintrayRepo("mausamy", "tmtyped")
    ),
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xlint:_,-missing-interpolator",
      "-Ywarn-dead-code",
      "-P:silencer:checkUnused"
      //      "-Xprint:typer"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin(`silencer-plugin`),
      `silencer-lib` % Provided
    )
  )
)

lazy val `msocket-root` = project
  .in(file("."))
  .aggregate(
    `portable-akka`.jvm,
    `portable-akka`.js,
    `msocket`,
    `example-service`
  )

//************* akka-api *****************************************************

lazy val `portable-akka` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Dummy)
  .jvmSettings(
    libraryDependencies ++= Seq(
      `akka-stream`,
      `akka-actor-typed`
    )
  )

//************* msocket *****************************************************

lazy val `msocket` = project.aggregate(
  `msocket-api`.jvm,
  `msocket-api`.js,
  `msocket-impl`,
  `msocket-impl-js`
)

lazy val `msocket-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("msocket/msocket-api"))
  .dependsOn(`portable-akka`)
  .settings(
    libraryDependencies ++= Seq(
      `borer-core`.value,
      `borer-derivation`.value
    )
  )

lazy val `msocket-impl` = project
  .in(file("msocket/msocket-impl"))
  .dependsOn(`msocket-api`.jvm)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http`,
      `borer-compat-akka`,
      `prometheus-akka-http`
    )
  )

lazy val `msocket-impl-js` = project
  .in(file("msocket/msocket-impl-js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`msocket-api`.js)
  .settings(
    libraryDependencies ++= Seq(
      `eventsource`.value,
      `rsocket-websocket-client`.value,
      `scalajs-dom`.value
    )
  )

lazy val `msocket-impl-rsocket` = project
  .in(file("msocket/msocket-impl-rsocket"))
  .dependsOn(`msocket-impl`)
  .settings(
    libraryDependencies ++= Seq(
      `rsocket-transport-netty`,
      `rsocket-core`
    )
  )

//************* example-service *****************************************************

lazy val `example-service` = project.aggregate(
  `example-service-api`.jvm,
  `example-service-api`.js,
  `example-service-impl`,
  `example-service-server`,
  `example-service-app-jvm`,
  `example-service-app-js`,
  `example-service-test`
)

lazy val `example-service-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("example-service/example-service-api"))
  .dependsOn(`msocket-api`)

lazy val `example-service-impl` = project
  .in(file("example-service/example-service-impl"))
  .dependsOn(`example-service-api`.jvm)

lazy val `example-service-server` = project
  .in(file("example-service/example-service-server"))
  .dependsOn(`example-service-impl`, `msocket-impl`, `msocket-impl-rsocket`)
  .settings(
    libraryDependencies ++= Seq(
      `akka-http-cors`,
      `scalatest`.value     % Test,
      `akka-http-testkit`   % Test,
      `akka-stream-testkit` % Test
    )
  )

lazy val `example-service-app-jvm` = project
  .in(file("example-service/example-service-app-jvm"))
  .dependsOn(`example-service-api`.jvm, `msocket-impl`, `msocket-impl-rsocket`)
  .settings(
    libraryDependencies ++= Seq(
      `scalatest`.value % Test
    )
  )

lazy val `example-service-app-js` = project
  .in(file("example-service/example-service-app-js"))
  .dependsOn(`example-service-api`.js, `msocket-impl-js`)
  .configure(baseJsSettings, bundlerSettings)
  .settings(
    npmDependencies in Compile ++= Seq(
      "eventsource"              -> "1.0.7",
      "can-ndjson-stream"        -> "1.0.2",
      "rsocket-websocket-client" -> "0.0.18"
    ),
    libraryDependencies ++= Seq(
      `scalatest`.value % Test
    )
  )

lazy val `example-service-test` = project
  .in(file("example-service/example-service-test"))
  .dependsOn(`example-service-server`, `example-service-app-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `akka-stream-testkit` % Test,
      `scalatest`.value     % Test
    )
  )

///////////////

lazy val baseJsSettings: Project => Project =
  _.enablePlugins(ScalaJSPlugin)
    .settings(
      scalaJSUseMainModuleInitializer := true,
      scalaJSModuleKind := ModuleKind.CommonJSModule,
      scalaJSLinkerConfig ~= { _.withESFeatures(_.withUseECMAScript2015(true)) },
      /* disabled because it somehow triggers many warnings */
      emitSourceMaps := false,
      /* in preparation for scala.js 1.0 */
      scalacOptions += "-P:scalajs:sjsDefinedByDefault",
      /* for ScalablyTyped */
      resolvers += Resolver.bintrayRepo("oyvindberg", "ScalablyTyped")
    )

lazy val start = TaskKey[Unit]("start")

lazy val bundlerSettings: Project => Project =
  _.enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      start := {
        (Compile / fastOptJS / startWebpackDevServer).value
        val indexFrom = baseDirectory.value / "html" / "index.html"
        val indexTo   = (Compile / fastOptJS / crossTarget).value / "index.html"
        Files.copy(indexFrom.toPath, indexTo.toPath, REPLACE_EXISTING)
      },
      /* Specify current versions and modes */
      startWebpackDevServer / version := "3.8.0",
      webpack / version := "4.39.3",
      Compile / fastOptJS / webpackExtraArgs += "--mode=development",
      Compile / fullOptJS / webpackExtraArgs += "--mode=production",
      Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
      Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production",
      useYarn := true
    )
