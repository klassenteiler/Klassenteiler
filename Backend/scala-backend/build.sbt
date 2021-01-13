name := """Klassenteiler"""
organization := "com.cogscigang"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.13.3"

maintainer := "anton@laukemper.it"

libraryDependencies ++= Seq(
guice,
jdbc % Test,
"org.postgresql" % "postgresql" % "42.1.0",
"org.apache.commons" % "commons-math3" % "3.3",
"com.typesafe.play" %% "play-slick" % "5.0.0",
"com.typesafe.play" %% "play-json" % "2.8.1",
"com.typesafe.slick" %% "slick-codegen" % "3.3.3",
"com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
"org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
)
