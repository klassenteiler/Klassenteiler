name := """Klassenteiler"""
organization := "com.cogscigang"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.13.3"

maintainer := "anton@laukemper.it"

libraryDependencies ++= Seq(
guice,
jdbc,
"org.postgresql" % "postgresql" % "42.1.0",
"com.typesafe.play" %% "play-slick" % "5.0.0",
"com.typesafe.play" %% "play-json" % "2.8.1",
"com.typesafe.slick" %% "slick-codegen" % "3.3.3",
"com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
"org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.cogscigang.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.cogscigang.binders._"
