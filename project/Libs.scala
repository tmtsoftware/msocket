import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {

  val `silencer-plugin` = "com.github.ghik" % "silencer-plugin" % "1.4.4" cross CrossVersion.full
  val `silencer-lib`    = "com.github.ghik" % "silencer-lib"    % "1.4.4" cross CrossVersion.full

  val `akka-stream`         = "com.typesafe.akka" %% "akka-stream"         % "2.6.1"
  val `akka-actor-typed`    = "com.typesafe.akka" %% "akka-actor-typed"    % "2.6.1"
  val `akka-stream-testkit` = "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.1"

  val `akka-http`         = "com.typesafe.akka" %% "akka-http"         % "10.1.11"
  val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11"

  val `borer-core`        = dep("io.bullet" %%% "borer-core" % "1.3.0-SNAPSHOT")
  val `borer-derivation`  = dep("io.bullet" %%% "borer-derivation" % "1.3.0-SNAPSHOT")
  val `borer-compat-akka` = "io.bullet" %% "borer-compat-akka" % "1.3.0-SNAPSHOT"

  val `akka-http-cors` = "ch.megard" %% "akka-http-cors" % "0.4.2"
  val `scalatest`      = dep("org.scalatest" %%% "scalatest" % "3.1.0")
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "0.9.8")

  val `std`                      = dep("com.github.tmtsoftware.tmt-typed" %%% "std"                      % "189686e")
  val `eventsource`              = dep("com.github.tmtsoftware.tmt-typed" %%% "eventsource"              % "189686e")
  val `rsocket-websocket-client` = dep("com.github.tmtsoftware.tmt-typed" %%% "rsocket-websocket-client" % "189686e")

  val `rsocket-core`            = "io.rsocket"                 % "rsocket-core"            % "1.0.0-RC5"
  val `rsocket-transport-netty` = "io.rsocket"                 % "rsocket-transport-netty" % "1.0.0-RC5"
  val `case-app`                = "com.github.alexarchambault" %% "case-app"               % "2.0.0-M9"

}

object csw {
  val cswVersion  = "05596e4"
  private val Org = "com.github.tmtsoftware.csw"

  val `csw-aas-http`        = Org %% "csw-aas-http"        % cswVersion
  val `csw-aas-installed`   = Org %% "csw-aas-installed"   % cswVersion
  val `csw-location-client` = Org %% "csw-location-client" % cswVersion
}
