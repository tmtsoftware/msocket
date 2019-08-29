import sbt._
import sbt.Def.{setting => dep}
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val `silencer-lib`    = "com.github.ghik" %% "silencer-lib"    % "1.4.1"
  val `silencer-plugin` = "com.github.ghik" %% "silencer-plugin" % "1.4.1"

  val `akka-stream`         = "com.typesafe.akka" %% "akka-stream"         % "2.5.23"
  val `akka-stream-testkit` = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23"

  val `akka-http`            = "com.typesafe.akka" %% "akka-http"            % "10.1.9"
  val `akka-http-spray-json` = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
  val `akka-http-testkit`    = "com.typesafe.akka" %% "akka-http-testkit"    % "10.1.9"

  val `borer-core`        = dep("io.bullet" %%% "borer-core" % "0.11.1")
  val `borer-derivation`  = dep("io.bullet" %%% "borer-derivation" % "0.11.1")
  val `borer-compat-akka` = "io.bullet" %% "borer-compat-akka" % "0.11.1"

  val `akka-http-cors` = "ch.megard" %% "akka-http-cors" % "0.4.1"
  val `scalatest`      = dep("org.scalatest" %%% "scalatest" % "3.0.8")
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "0.9.7")

  val `eventsource` = dep("com.github.mushtaq.scalably-typed" %%% "eventsource" % "de25c8e")
  val `std`         = dep("com.github.mushtaq.scalably-typed" %%% "std"         % "de25c8e")
}
