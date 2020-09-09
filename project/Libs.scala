import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {
  // 1.0.0-M1 does not work with Scala.js js yet
  val `scala-async` = "org.scala-lang.modules" %% "scala-async" % "0.10.0"

  val `akka-stream`              = "com.typesafe.akka" %% "akka-stream"              % "2.6.8"
  val `akka-actor-typed`         = "com.typesafe.akka" %% "akka-actor-typed"         % "2.6.8"
  val `akka-actor-testkit-typed` = "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.8"
  val `akka-stream-testkit`      = "com.typesafe.akka" %% "akka-stream-testkit"      % "2.6.8"

  val `akka-http`         = "com.typesafe.akka" %% "akka-http"         % "10.2.0"
  val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % "10.2.0"

  val `borer-core`        = dep("io.bullet" %%% "borer-core" % "1.6.1")
  val `borer-derivation`  = dep("io.bullet" %%% "borer-derivation" % "1.6.1")
  val `borer-compat-akka` = "io.bullet" %% "borer-compat-akka" % "1.6.1"

  val `akka-http-cors` = "ch.megard"         %% "akka-http-cors" % "1.1.0"
  val scalatest        = dep("org.scalatest" %%% "scalatest" % "3.2.2")
  val `selenium-3-141` = "org.scalatestplus" %% "selenium-3-141" % "3.2.2.0"
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "1.1.0")

  val eventsource                = dep("org.scalablytyped" %%% "eventsource" % "1.1.4-5269e8")
  val `rsocket-websocket-client` = dep("org.scalablytyped" %%% "rsocket-websocket-client" % "0.0.3-280a1e")

  val `rsocket-core`            = "io.rsocket" % "rsocket-core"            % "1.0.1"
  val `rsocket-transport-netty` = "io.rsocket" % "rsocket-transport-netty" % "1.0.1"
}

object Prometheus {
  val Version             = "0.9.0"
  val simpleclient        = "io.prometheus" % "simpleclient"        % Version
  val simpleclient_common = "io.prometheus" % "simpleclient_common" % Version
}
