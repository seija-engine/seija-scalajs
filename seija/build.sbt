enablePlugins(ScalaJSPlugin)
name := "seija"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.3"
libraryDependencies += "biz.enef" %%% "slogging" % "0.6.2"

scalaJSUseMainModuleInitializer := true