addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % "0.4.1")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.0.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.28")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.15.0-0.6")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")

libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"

addSbtCoursier

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
