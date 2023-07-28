import Libs._
import org.openqa.selenium.chrome.ChromeOptions
import org.scalajs.jsenv.selenium.SeleniumJSEnv
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

inThisBuild(
  Seq(
    scalaVersion      := "3.3.0",
    // jitpack provides the env variable VERSION=<version being built> # A tag or commit
    // we make use of it so that the version in class metadata (this.getClass.getPackage.getSpecificationVersion)
    // and the maven repo match
    version           := sys.env.getOrElse("JITPACK_VERSION", "0.1.0-SNAPSHOT"),
    organization      := "com.github.tmtsoftware.msocket",
    organizationName  := "ThoughtWorks",
    scalafmtOnCompile := true,
    resolvers += "jitpack" at "https://jitpack.io",
    resolvers += "Apache Pekko Staging".at("https://repository.apache.org/content/groups/staging"),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation"
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
  `portable-pekko`.jvm,
  `portable-pekko`.js
)

lazy val `portable-observer` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("portable/portable-observer"))

lazy val `portable-pekko` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Dummy)
  .in(file("portable/portable-pekko"))
  .dependsOn(`portable-observer`)
  .jvmSettings(
    libraryDependencies ++= Seq(
      `pekko-stream`,
      `pekko-actor-typed`
    )
  )
  .jsSettings(
    libraryDependencies += `scala-js-macrotask-executor`.value
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
  .dependsOn(`portable-pekko`)
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
      `pekko-http`,
      `borer-compat-pekko`,
      scalatest.value            % Test,
      `pekko-actor-testkit-typed` % Test,
      `pekko-http-testkit`        % Test
    )
  )

lazy val `msocket-rsocket` = project
  .in(file("msocket/msocket-rsocket"))
  .dependsOn(`msocket-jvm`)
  .settings(
    libraryDependencies ++= Seq(
      `rsocket-transport-netty`,
      `rsocket-core`,
      Libs.`scala-java8-compat`
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
      `pekko-http`,
      `pekko-http-cors`,
      scalatest.value       % Test,
      `pekko-http-testkit`   % Test,
      `pekko-stream-testkit` % Test
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
      `pekko-stream-testkit` % Test,
      scalatest.value       % Test
    )
  )

lazy val `example-client-js` = project
  .in(file("example/example-client-js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(`example-service-api`.js, `msocket-js`)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule).withSourceMap(false) },
    Test / jsEnv                    := {
      new SeleniumJSEnv(
        new ChromeOptions().setHeadless(true),
        seleniumConfig(3000, baseDirectory.value.getAbsolutePath)
      )
    },
    libraryDependencies ++= Seq(
      scalatest.value % Test,
      `dotty-cps-async`.value,
      `shim-scala-async-dotty-cps-async`
//      `scala-async`.value
    )
  )

def seleniumConfig(port: Int, base: String): SeleniumJSEnv.Config = {
  import _root_.io.github.bonigarcia.wdm.WebDriverManager
  //  WebDriverManager.chromedriver().setup()
  val contentDirName = "target/selenium"
  val webRoot        = s"http://localhost:$port/$contentDirName/"
  val contentDir     = s"$base/$contentDirName"
  SeleniumJSEnv
    .Config()
    .withMaterializeInServer(contentDir, webRoot)
}
