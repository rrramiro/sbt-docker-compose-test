name := "basic"

version := "1.0.0"

scalaVersion := "2.12.15"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.8.7" % "test",
  "org.scalaj" %% "scalaj-http" % "2.4.2" % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test"
)

enablePlugins(DockerPlugin, PluginDockerCompose)

//Set the image creation Task to be the one used by sbt-docker
dockerComposeUp := {
  (dockerComposeUp dependsOn docker).value
}

Test / test := Def.taskDyn {
  val a: Task[Unit] = dockerComposeUp.taskValue
  val b: Task[Unit] = (Test / test).taskValue
  val c: Task[Unit] = dockerComposeDown.taskValue
  Def.task {
    ((a doFinally b) doFinally c).value
  }
}.tag(ConcurrentRestrictions.All).value

//TODO Test / testOptions += Tests.Argument( TestFrameworks.Specs2,"filesrunner.verbose")

Test / fork := true

Test / envVars ++= dockerInstances.value

docker / dockerfile := {
  new Dockerfile {
    val dockerAppPath = "/app/"
    val mainClassString = (Compile / mainClass).value.get
    val classpath = (Compile / fullClasspath).value
    from("openjdk:8")
    add(classpath.files, dockerAppPath)
    entryPoint("java", "-cp", s"$dockerAppPath:$dockerAppPath/*", s"$mainClassString")
  }
}

docker / imageNames := Seq(ImageName(
  repository = name.value.toLowerCase,
  tag = Some(version.value))
)