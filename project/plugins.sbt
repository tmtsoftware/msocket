addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % "0.5.1")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.4.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.1.1")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.18.0")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")

libraryDependencies += "com.sun.activation" % "javax.activation"     % "1.2.0"
libraryDependencies += "org.scala-js"      %% "scalajs-env-selenium" % "1.0.0"

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint:-unused,_",
  "-Ywarn-dead-code",
  "-Xfuture"
)

resolvers += Resolver.bintrayIvyRepo("rtimush", "sbt-plugin-snapshots")
addSbtPlugin("com.timushev.sbt" % "sbt-rewarn" % "0.0.1-15-3102b36")
