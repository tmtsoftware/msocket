import sbt._

object Dependencies {
  val `scalatest`       = "org.scalatest"     %% "scalatest"       % "3.0.8"
  val `akka-http`       = "com.typesafe.akka" %% "akka-http"       % "10.1.9"
  val `akka-stream`     = "com.typesafe.akka" %% "akka-stream"     % "2.5.23"
  val `silencer-lib`    = "com.github.ghik"   %% "silencer-lib"    % "1.4.1"
  val `silencer-plugin` = "com.github.ghik"   %% "silencer-plugin" % "1.4.1"
}

object Borer {
  val Version = "0.10.0"
  val Org     = "io.bullet"

  val `borer-core`        = Org %% "borer-core"        % Version
  val `borer-derivation`  = Org %% "borer-derivation"  % Version
  val `borer-compat-akka` = Org %% "borer-compat-akka" % Version
}
