ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "day-of-week-game",
      scalacOptions ++= ScalacOptions.allScalacOptions,
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "com.raquo"                  %%% "laminar"     % "16.0.0",
        "dev.zio"                    %%% "zio"         % "2.1.1",
        "dev.zio"                    %%% "zio-json"    % "0.6.2",
        "dev.zio"                    %%% "zio-prelude" % "1.0.0-RC25",
        "com.softwaremill.quicklens" %%% "quicklens"   % "1.9.6",
        // java.time library support for Scala.js
        "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.5.0",
      )
    )

Global / onChangedBuildSource := ReloadOnSourceChanges

addCommandAlias("c", "fastOptJS")
