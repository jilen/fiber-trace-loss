name := "fiber-trace-loss"

scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.0"
)

enablePlugins(PlayService)
