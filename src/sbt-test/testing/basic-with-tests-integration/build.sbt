
lazy val root = project.in(file("."))
  .enablePlugins(DockerPlugin, PluginDockerCompose)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "basic",
    version := "1.0.0",
    scalaVersion := "2.12.15",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "it",
      "org.scalaj" %% "scalaj-http" % "2.4.2" % "it",
      "org.pegdown" % "pegdown" % "1.6.0" % "it"
    ),
    dockerComposeUp := {
      (dockerComposeUp dependsOn docker).value
    },
    IntegrationTest / test := Def.taskDyn {
      val a: Task[Unit] = dockerComposeUp.taskValue
      val b: Task[Unit] = (IntegrationTest / test).taskValue
      val c: Task[Unit] = dockerComposeDown.taskValue
      Def.task {
        ((a doFinally b) doFinally c).value
      }
    }.tag(ConcurrentRestrictions.All).value,
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, dockerInstances.value.toSeq.map {
        case (key, value) => s"-D$key=$value"
      } : _*),
    docker / dockerfile := {
      new Dockerfile {
        val dockerAppPath = "/app/"
        val mainClassString = (Compile / mainClass).value.get
        val classpath = (Compile / fullClasspath).value
        from("openjdk:8")
        add(classpath.files, dockerAppPath)
        entryPoint("java", "-cp", s"$dockerAppPath:$dockerAppPath/*", s"$mainClassString")
      }
    },
    docker / imageNames := Seq(ImageName(
      repository = name.value.toLowerCase,
      tag = Some(version.value))
    )
  )
