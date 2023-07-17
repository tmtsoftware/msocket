import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.{setting => dep}
import sbt._

object Libs {
  val `scala-async` = dep("org.scala-lang.modules" %%% "scala-async" % "1.0.1")
  val `scala-java8-compat` = "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2" // BSD 3-clause "New" or "Revised" License

  private val pekkoVersion = "1.0.0"

  val `pekko-stream`              = "org.apache.pekko" %% "pekko-stream"              % pekkoVersion
  val `pekko-actor-typed`         = "org.apache.pekko" %% "pekko-actor-typed"         % pekkoVersion
  val `pekko-actor-testkit-typed` = "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion
  val `pekko-stream-testkit`      = "org.apache.pekko" %% "pekko-stream-testkit"      % pekkoVersion

  private val pekkoHttpVersion = "0.0.0+4470-61034832-SNAPSHOT"

  val `pekko-http`            = "org.apache.pekko" %% "pekko-http"            % pekkoHttpVersion
  val `pekko-http-cors`       = "org.apache.pekko" %% "pekko-http-cors"            % pekkoHttpVersion
  val `pekko-http-testkit` = "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpVersion

  private val borerVersion = "1.7.2-pekko"
  val `borer-core`         = dep("io.bullet" %%% "borer-core" % borerVersion)
  val `borer-derivation`   = dep("io.bullet" %%% "borer-derivation" % borerVersion)
  val `borer-compat-pekko`  = "io.bullet" %% "borer-compat-pekko" % borerVersion

//  val `pekko-http-cors` = "ch.megard"         %% "pekko-http-cors" % "1.1.2"
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
