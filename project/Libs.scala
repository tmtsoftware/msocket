import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {

  val `silencer-plugin` = "com.github.ghik" % "silencer-plugin" % "1.4.4" cross CrossVersion.full
  val `silencer-lib`    = "com.github.ghik" % "silencer-lib"    % "1.4.4" cross CrossVersion.full

  val `akka-stream`         = "com.typesafe.akka" %% "akka-stream"         % "2.6.3"
  val `akka-actor-typed`    = "com.typesafe.akka" %% "akka-actor-typed"    % "2.6.3"
  val `akka-stream-testkit` = "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.3"

  val `akka-http`         = "com.typesafe.akka" %% "akka-http"         % "10.1.11"
  val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11"

  val `borer-core`        = dep("io.bullet" %%% "borer-core" % "1.5.0")
  val `borer-derivation`  = dep("io.bullet" %%% "borer-derivation" % "1.5.0")
  val `borer-compat-akka` = "io.bullet" %% "borer-compat-akka" % "1.5.0"

  val `akka-http-cors` = "ch.megard" %% "akka-http-cors" % "0.4.2"
  val `scalatest`      = dep("org.scalatest" %%% "scalatest" % "3.1.1")
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "1.0.0")

  val `std`                      = dep("org.scalablytyped" %%% "std"                      % "3.8-6572c3")
  val `eventsource`              = dep("org.scalablytyped" %%% "eventsource"              % "1.1.2-f523b6")
  val `rsocket-websocket-client` = dep("org.scalablytyped" %%% "rsocket-websocket-client" % "0.0.3-4b68c6")

  val `rsocket-core`            = "io.rsocket" % "rsocket-core"            % "1.0.0-RC5"
  val `rsocket-transport-netty` = "io.rsocket" % "rsocket-transport-netty" % "1.0.0-RC5"

  val `prometheus-akka-http` = "com.lonelyplanet" %% "prometheus-akka-http" % "0.5.0"
}
