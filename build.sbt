lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    name := "sbt-docker-compose-test",
    organization := "fr.ramiro",
    scalaVersion := "2.12.15",
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "sbt" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "main" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "main-settings" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "collections" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "command" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "completion" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "core-macros" % sbtVersion.value % Provided,
      "org.scala-sbt" %% "task-system" % sbtVersion.value % Provided,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test
    ),
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Xfatal-warnings",
      "-Xlint:-infer-any,_", // -byname-implicit,
      // "-Xlog-implicits",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused:_"
    ),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    publishTo := {
      val nexus = "https://oss.sonatype.org"
      if (isSnapshot.value)
        Some("snapshots".at(s"$nexus/content/repositories/snapshots"))
      else
        Some("releases".at(s"$nexus/service/local/staging/deploy/maven2"))
    },
    publishMavenStyle := true,
    Test / publishArtifact := false,
    Global / onChangedBuildSource := ReloadOnSourceChanges
  )

publishMavenStyle := true
licenses := Seq("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause"))
homepage := Some(url("https://github.com/rrramiro/sbt-docker-compose-test"))
scmInfo := Some(
  ScmInfo(url("https://github.com/sbt/sbt-native-packager"), "scm:git@github.com:sbt/sbt-native-packager.git")
)
developers := List(
  Developer(
    id = "rrramiro",
    name = "Ramiro Calle",
    email = "", //todo
    url = url("https://github.com/rrramiro")
  )
)
