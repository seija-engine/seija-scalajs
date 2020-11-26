enablePlugins(ScalaJSPlugin)
name := "seija"
scalacOptions ++= Seq(
  "-Ymacro-annotations"
)
version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.3"

scalaJSUseMainModuleInitializer := true