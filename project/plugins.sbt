addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % "0.5.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.0.1")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.17.0")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")

libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  //"-Xfatal-warnings",
  "-Xlint:-unused,_",
  "-Ywarn-dead-code",
  "-Xfuture"
)
