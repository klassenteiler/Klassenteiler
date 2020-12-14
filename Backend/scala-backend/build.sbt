name := """Klassentrenner"""
organization := "com.cogscigang"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.13.3"

maintainer := "anton@laukemper.it"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.cogscigang.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.cogscigang.binders._"
