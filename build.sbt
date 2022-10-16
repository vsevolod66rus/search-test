import sbt.Keys.libraryDependencies

import java.time.Instant

ThisBuild / scalaVersion := "2.13.4"

name := "sof-search-project"

lazy val commonSettings = Seq(
  addCompilerPlugin(Dependencies.kindProjector),
  addCompilerPlugin(Dependencies.betterMonadicFor),
  scalafmtOnCompile := true,
  Global / onChangedBuildSource := ReloadOnSourceChanges,
  Global / cancelable := true,
  Test / fork := true,
  turbo := true,
  scalacOptions += "-P:silencer:pathFilters=.*[/]src_managed[/].*",
  libraryDependencies += Dependencies.cats,
  libraryDependencies += Dependencies.catsEffect,
  libraryDependencies ++= Dependencies.silencer
)

lazy val applicationSettings = Seq(
  Universal / mappings += ((Compile / resourceDirectory).value / "application.conf") -> "conf/application.conf",
  Universal / javaOptions += "-J-Xmx2G",
  publish / skip := true
)

lazy val sofSearchApp = project
  .in(file("sof-search-app"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)
  .settings(commonSettings)
  .settings(applicationSettings)
  .settings(
    name := "sof-search-app",
    buildInfoKeys += BuildInfoKey.action("buildTime")(Instant.now().toString),
    buildInfoKeys += BuildInfoKey.action("commit")(git.gitHeadCommit.value),
    Universal / mappings += ((Compile / resourceDirectory).value / "params.conf") -> "conf/params.conf",
    Universal / javaOptions += "-J-Xmx2G",
    scalacOptions -= "-Xfatal-warnings",
    publish / skip := true,
    libraryDependencies += Dependencies.pureconfig,
    libraryDependencies += Dependencies.pureconfigCE,
    libraryDependencies += Dependencies.cats,
    libraryDependencies += Dependencies.catsEffect,
    libraryDependencies += Dependencies.catsEffectStd,
    libraryDependencies ++= Dependencies.http4s,
    libraryDependencies += Dependencies.http4sCirce,
    libraryDependencies += Dependencies.fs2,
    libraryDependencies += Dependencies.logback,
    libraryDependencies += Dependencies.jclOverSlf4j,
    libraryDependencies += Dependencies.catsRetry,
    libraryDependencies += Dependencies.magnolia,
    libraryDependencies += Dependencies.tapirCore,
    libraryDependencies ++= Dependencies.tapir,
    libraryDependencies += Dependencies.tapirRefined,
    libraryDependencies ++= Dependencies.tapirServer,
    libraryDependencies += Dependencies.tapirSttpClient,
    libraryDependencies += Dependencies.sttpCore,
    libraryDependencies += Dependencies.sttpClient3Core,
    libraryDependencies ++= Dependencies.circe,
    libraryDependencies += Dependencies.circeFs2,
    libraryDependencies += Dependencies.circeEnumeratum,
    libraryDependencies += Dependencies.circeGenericExtras,
    libraryDependencies += Dependencies.log4Cats,
    libraryDependencies += Dependencies.munit,
    libraryDependencies += Dependencies.scalacheck,
    libraryDependencies += Dependencies.mockito
  )
  .settings(
    excludeDependencies ++= Seq(
      ExclusionRule("commons-logging", "commons-logging"),
      ExclusionRule("log4j", "log4j"),
      ExclusionRule("org.apache.logging.log4j", "log4j-core"),
      ExclusionRule("org.apache.logging.log4j", "log4j-api")
    )
  )
