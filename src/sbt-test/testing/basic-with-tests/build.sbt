name := "basic"

version := "1.0.0"

scalaVersion := "2.12.15"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalaj" %% "scalaj-http" % "2.4.2" % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test"
)

enablePlugins(DockerPlugin, PluginDockerCompose)

//Only execute tests tagged as the following
testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-n", "DockerComposeTag" )

//Specify that an html report should be created for the test pass
testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h","target/htmldir")

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

testOptions += {
  val info = dockerInstances.value
  val args = info.toSeq.map {
    case (key, value) => s"-D$key=$value"
  }

  Tests.Argument(TestFrameworks.ScalaTest, args :_*)
}


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

Global / onChangedBuildSource := ReloadOnSourceChanges
