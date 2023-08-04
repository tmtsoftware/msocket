import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {
  val `scala-js-macrotask-executor` = dep("org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.1")
  val `dotty-cps-async` = dep("com.github.rssh" %%% "dotty-cps-async" % "0.9.17")
  val `shim-scala-async-dotty-cps-async` = "com.github.rssh" %% "shim-scala-async-dotty-cps-async" % "0.9.17"

  private val pekkoVersion = "1.0.1"
    val pekkoOrg = "org.apache.pekko"
  val `pekko-stream`              = pekkoOrg %% "pekko-stream"              % pekkoVersion
  val `pekko-actor-typed`         = pekkoOrg %% "pekko-actor-typed"         % pekkoVersion
  val `pekko-actor-testkit-typed` = pekkoOrg %% "pekko-actor-testkit-typed" % pekkoVersion
  val `pekko-stream-testkit`      = pekkoOrg %% "pekko-stream-testkit"      % pekkoVersion

  private val pekkoHttpVersion = "1.0.0"
  val pekkoHttpOrg = "org.apache.pekko"
  val `pekko-http` = pekkoHttpOrg %% "pekko-http" % pekkoHttpVersion
  val `pekko-http-cors` = pekkoHttpOrg %% "pekko-http-cors" % pekkoHttpVersion
  val `pekko-http-testkit` = pekkoHttpOrg %% "pekko-http-testkit" % pekkoHttpVersion

  private val borerVersion = "687c9de"
  //  val borerOrg     = "io.bullet"
  val borerOrg = "com.github.tmtsoftware.borer"
  val `borer-core` = dep(borerOrg %%% "borer-core" % borerVersion)
  val `borer-derivation` = dep(borerOrg %%% "borer-derivation" % borerVersion)
  val `borer-compat-pekko` = borerOrg %% "borer-compat-pekko" % borerVersion

  val scalatest        = dep("org.scalatest" %%% "scalatest" % "3.2.16")
  val `selenium-3-141` = "org.scalatestplus" %% "selenium-3-141" % "3.2.10.0"
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "2.6.0")

  val `tmt-typed` = dep("com.github.mushtaq.tmt-typed" %%% "tmt-typed" % "2548bb6")

  private val rsocketVersion    = "1.1.4"
  val `rsocket-core`            = "io.rsocket" % "rsocket-core"            % rsocketVersion
  val `rsocket-transport-netty` = "io.rsocket" % "rsocket-transport-netty" % rsocketVersion
}

object Prometheus {
  val Version             = "0.16.0"
  val simpleclient        = "io.prometheus" % "simpleclient"        % Version
  val simpleclient_common = "io.prometheus" % "simpleclient_common" % Version
}
