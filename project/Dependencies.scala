import sbt._
import sbt.Def.{setting => dep}
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val `scalatest`            = dep("org.scalatest" %%% "scalatest" % "3.0.8")
  val `akka-http`            = "com.typesafe.akka" %% "akka-http" % "10.1.9"
  val `akka-http-spray-json` = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
  val `akka-http-testkit`    = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.9"
  val `akka-stream`          = "com.typesafe.akka" %% "akka-stream" % "2.5.23"
  val `akka-stream-testkit`  = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23"
  val `silencer-lib`         = "com.github.ghik" %% "silencer-lib" % "1.4.1"
  val `silencer-plugin`      = "com.github.ghik" %% "silencer-plugin" % "1.4.1"
  val `scalajs-dom`          = dep("org.scala-js" %%% "scalajs-dom" % "0.9.7")
}

object Borer {
  val Version = "0.11.1"
  val Org     = "io.bullet"

  val `borer-core`        = Org %% "borer-core"        % Version
  val `borer-derivation`  = Org %% "borer-derivation"  % Version
  val `borer-compat-akka` = Org %% "borer-compat-akka" % Version
}
