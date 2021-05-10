import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {
  // 1.0.0-M1 does not work with Scala.js js yet
  val `scala-async` = "org.scala-lang.modules" %% "scala-async" % "0.10.0"

  val `akka-stream`              = "com.typesafe.akka" %% "akka-stream"              % "2.6.14"
  val `akka-actor-typed`         = "com.typesafe.akka" %% "akka-actor-typed"         % "2.6.14"
  val `akka-actor-testkit-typed` = "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.14"
  val `akka-stream-testkit`      = "com.typesafe.akka" %% "akka-stream-testkit"      % "2.6.14"

  val `akka-http`         = "com.typesafe.akka" %% "akka-http"         % "10.2.4"
  val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % "10.2.4"

  val `borer-core`        = dep("io.bullet" %%% "borer-core" % "1.7.2")
  val `borer-derivation`  = dep("io.bullet" %%% "borer-derivation" % "1.7.2")
  val `borer-compat-akka` = "io.bullet" %% "borer-compat-akka" % "1.7.2"

  val `akka-http-cors` = "ch.megard"         %% "akka-http-cors" % "1.1.1"
  val scalatest        = dep("org.scalatest" %%% "scalatest" % "3.2.8")
  val `selenium-3-141` = "org.scalatestplus" %% "selenium-3-141" % "3.2.2.0"
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "1.1.0")

  val `tmt-typed` = dep("com.github.mushtaq.tmt-typed" %%% "tmt-typed" % "902393a")

  val `rsocket-core`            = "io.rsocket" % "rsocket-core"            % "1.1.0"
  val `rsocket-transport-netty` = "io.rsocket" % "rsocket-transport-netty" % "1.1.0"
}

object Prometheus {
  val Version             = "0.10.0"
  val simpleclient        = "io.prometheus" % "simpleclient"        % Version
  val simpleclient_common = "io.prometheus" % "simpleclient_common" % Version
}
