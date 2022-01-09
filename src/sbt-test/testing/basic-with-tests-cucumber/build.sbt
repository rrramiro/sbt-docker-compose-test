name := "basic-cucumber"

version := "1.0.0"

scalaVersion := "2.12.15"

enablePlugins(DockerPlugin, PluginDockerCompose, CucumberPlugin)

val cucumberVersion = "4.3.0"

libraryDependencies ++= {
    Seq("core", "jvm", "junit").map { suffix =>
      "io.cucumber" % s"cucumber-$suffix" % cucumberVersion % "test"
    } ++ Seq(
    "io.cucumber" %% "cucumber-scala" % cucumberVersion % "test",
    "org.scalactic" %% "scalactic" % "3.0.4" % "test",
    "org.scalatest" %% "scalatest" % "3.0.4" % ("test->*"),
    //"org.scalaj" %% "scalaj-http" % "2.4.2",
    "org.pegdown" % "pegdown" % "1.6.0" % ("test->*"),
    "junit" % "junit" % "4.12" % "test"
  )
}

CucumberPlugin.glues := List("classpath:")
// TODO CucumberPlugin.features += "classpath:"

//Set the image creation Task to be the one used by sbt-docker
dockerComposeUp := {
  (dockerComposeUp dependsOn docker).value
}

CucumberPlugin.cucumber := Def.taskDyn {
  val a: Task[Unit] = dockerComposeUp.taskValue
  val b: Task[Unit] = CucumberPlugin.cucumber.tag(ConcurrentRestrictions.All).toTask("").taskValue
  val c: Task[Unit] = dockerComposeDown.taskValue
  Def.task {
    ((a doFinally b) doFinally c).value
  }
}.tag(ConcurrentRestrictions.All).value


//TODO CucumberPlugin.envProperties := Map.empty[String, String]

docker / imageNames := Seq(ImageName(
  repository = name.value.toLowerCase,
  tag = Some(version.value))
)

// create a docker file with a file /inputs/example.input
docker / dockerfile := {

  val classpath: Classpath = (Test / fullClasspath).value
  sLog.value.debug(s"Classpath is ${classpath.files.mkString("\n")}\n")

  new Dockerfile {
    val dockerAppPath = "/app/"
    from("openjdk:8")
    add(classpath.files, dockerAppPath)

    entryPoint("java", "-cp", s"$dockerAppPath:$dockerAppPath*", "example.CalculatorServer")
  }
}

Global / onChangedBuildSource := ReloadOnSourceChanges
