name := "tagless-play"

version := "0.1"

scalaVersion := "2.12.8"
// Change the sbt plugin to use the local Play build (2.6.0-SNAPSHOT)
lazy val `tagless-play` = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(ws)

libraryDependencies ++= Seq(
  "org.typelevel"     %% "cats-core"                % "1.6.0",
  "org.typelevel"     %% "cats-effect"              % "1.2.0",
  "org.reactivemongo" %% "reactivemongo-play-json"  % "0.16.0-play26",
)