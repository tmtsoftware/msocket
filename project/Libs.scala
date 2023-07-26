import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {
  val `scala-async` = dep("org.scala-lang.modules" %%% "scala-async" % "1.0.1")
  val `scala-java8-compat` = "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" // BSD 3-clause "New" or "Revised" License

  private val pekkoVersion = "1.0.1"
    val pekkoOrg = "org.apache.pekko"
//  val pekkoOrg = "com.github.apache.incubator-pekko"
  val `pekko-stream`              = pekkoOrg %% "pekko-stream"              % pekkoVersion
  val `pekko-actor-typed`         = pekkoOrg %% "pekko-actor-typed"         % pekkoVersion
  val `pekko-actor-testkit-typed` = pekkoOrg %% "pekko-actor-testkit-typed" % pekkoVersion
  val `pekko-stream-testkit`      = pekkoOrg %% "pekko-stream-testkit"      % pekkoVersion

  private val pekkoHttpVersion = "1.0.0-RC2"
//  val pekkoHttpOrg = "org.apache.pekko"
  val pekkoHttpOrg = "com.github.apache.incubator-pekko-http"
  val `pekko-http` = pekkoHttpOrg %% "pekko-http" % pekkoHttpVersion
  val `pekko-http-cors` = pekkoHttpOrg %% "pekko-http-cors" % pekkoHttpVersion
  val `pekko-http-testkit` = pekkoHttpOrg %% "pekko-http-testkit" % pekkoHttpVersion

  private val borerVersion = "d59e5fd"
  //  val borerOrg     = "io.bullet"
  val borerOrg = "com.github.tmtsoftware.borer"
  val `borer-core` = dep(borerOrg %%% "borer-core" % borerVersion)
  val `borer-derivation` = dep(borerOrg %%% "borer-derivation" % borerVersion)
  val `borer-compat-pekko` = borerOrg %% "borer-compat-pekko" % borerVersion

  val scalatest        = dep("org.scalatest" %%% "scalatest" % "3.2.10")
  val `selenium-3-141` = "org.scalatestplus" %% "selenium-3-141" % "3.2.10.0"
  val `scalajs-dom`    = dep("org.scala-js" %%% "scalajs-dom" % "2.1.0")

  val `tmt-typed` = dep("com.github.mushtaq.tmt-typed" %%% "tmt-typed" % "eae7acb")

  private val rsocketVersion    = "1.1.1"
  val `rsocket-core`            = "io.rsocket" % "rsocket-core"            % rsocketVersion
  val `rsocket-transport-netty` = "io.rsocket" % "rsocket-transport-netty" % rsocketVersion
}

object Prometheus {
  val Version             = "0.14.1"
  val simpleclient        = "io.prometheus" % "simpleclient"        % Version
  val simpleclient_common = "io.prometheus" % "simpleclient_common" % Version
}
