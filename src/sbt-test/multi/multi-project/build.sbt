name := "multi-project"

version := "1.0.0"

scalaVersion := "2.12.15"

enablePlugins(PluginDockerCompose)

docker := {
  (sample1 / docker).value
  (sample2 / docker).value
}

dockerComposeUp := {
  (dockerComposeUp dependsOn docker).value
}

val dockerAppPath = "/app/"

lazy val sample1 = project.
  enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    docker / dockerfile := {
      new Dockerfile {
        val mainClassString = (Compile / mainClass).value.get
        val classpath = (Compile / fullClasspath).value
        from("openjdk:8")
        add(classpath.files, dockerAppPath)
        entryPoint("java", "-cp", s"$dockerAppPath:$dockerAppPath/*", s"$mainClassString")
      }
    },
    docker / imageNames := Seq(ImageName(
    repository = name.value.toLowerCase,
    tag = Some("latest"))
    )
  )

lazy val sample2 = project.
  enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    docker / dockerfile := {
      new Dockerfile {
        val mainClassString = (Compile / mainClass).value.get
        val classpath = (Compile / fullClasspath).value
        from("openjdk:8")
        add(classpath.files, dockerAppPath)
        entryPoint("java", "-cp", s"$dockerAppPath:$dockerAppPath/*", s"$mainClassString")
      }
    },
    docker / imageNames := Seq(ImageName(
      repository = name.value.toLowerCase,
      tag = Some("latest"))
    )
  )