addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % "0.6.4")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.4.6")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.2.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.11.0")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")
addSbtPlugin("com.timushev.sbt"   % "sbt-rewarn"               % "0.1.3")

libraryDependencies += "com.sun.activation"   % "javax.activation"     % "1.2.0"
libraryDependencies += "io.github.bonigarcia" % "webdrivermanager"     % "5.3.0"
libraryDependencies += "org.scala-js"        %% "scalajs-env-selenium" % "1.1.1"

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
