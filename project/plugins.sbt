addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % "0.5.2")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.4.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.5.1")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")
addSbtPlugin("com.timushev.sbt"   % "sbt-rewarn"               % "0.1.3")

libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"

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

resolvers += "jitpack" at "https://jitpack.io"
// this is a sbt-plugin but do not use addSbtPlugin because it is being resolved via jitpack
libraryDependencies += "com.github.mushtaq.sbt-snowpack" % "sbt-snowpack" % "5bceb32"
