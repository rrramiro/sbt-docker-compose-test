package fr.ramiro.sbt.docker

import sbt._
import sbt.Keys._
import sbt.internal.util.complete.Parser.token

object PluginDockerCompose extends AutoPlugin with DockerComposeCommands {

  object autoImport {

    val composeFile = settingKey[File](
      "Specify the full path to the Compose File to use to create your test instance. It defaults to docker-compose.yml in your resources folder."
    )

    val dockerComposeInstances = taskKey[Unit](
      "Prints a table of information for all running Docker Compose instances."
    )

    val dockerInstances = taskKey[Map[String, String]](
      "Get exposed ports and container ids" // TODO
    )

    val dockerComposeUp =
      taskKey[Unit]("Starts a local Docker Compose instance.")

    val dockerComposeStop = taskKey[Unit](
      "Stops all local Docker Compose instances started in this sbt project."
    )

    val dockerComposeDown = taskKey[Unit](
      "Stops all local Docker Compose instances started in this sbt project."
    )

    val dockerEnvironmentVars = settingKey[Map[String, String]](
      "A Map[String,String] of variables to substitute in your docker-compose file. These are substituted by the plugin and not using environment variables."
    )
  }
  import autoImport._

  private val dockerComposeFileName = "docker-compose.yml"

  override def projectSettings: Seq[Setting[_]] = Seq(
    composeFile := {
      Seq(
        (Compile / resourceDirectory).value,
        baseDirectory.value
      ).map(a => a / dockerComposeFileName)
        .find(_.exists())
        .getOrElse(throw new Exception("error")) // TODO error
    },
    dockerComposeUp := {
      // Project.extract(state.value)
      dockerComposeUpCmd(
        composeFile.value,
        dockerEnvironmentVars.value
      )
    },
    dockerComposeStop := {
      dockerComposeStopCmd(composeFile.value)
    },
    dockerComposeDown := {
      dockerComposeDownCmd(composeFile.value)
    },
    dockerInstances := {
      listServices(composeFile.value)
        .foldLeft(Map.empty[String, String]) { case (acc, service) =>
          acc ++ dockerServiceInfo(
            composeFile.value,
            service
          )
        }
    },
    dockerComposeInstances := {
      listServices(composeFile.value).foreach { service =>
        val dockerId = dockerComposeDockerId(
          composeFile.value,
          service
        )
        println(s"$service -> $dockerId")
      }
    },
    dockerEnvironmentVars := {
      Map.empty[String, String]
    },
    commands ++= Seq(
      dockerRemoveImageCommand
    )
  )

  private def listServices(dockerComposeFile: File): Seq[String] =
    dockerComposeServices(dockerComposeFile)

  private val portMappingPattern = """(\d+)(\S+) -> (.*[:]{1,3}\d+)""".r

  def parseDockerPorts(
      service: String,
      exposedPorts: String
  ): Map[String, String] =
    exposedPorts.linesIterator
      .filterNot(_.contains(":::"))
      .collect { case portMappingPattern(container, proto, hostPort) =>
        val protocol = proto.replaceAll("(?i)/tcp", "")
        s"$service:$container$protocol" -> hostPort
      }
      .toMap

  private def dockerServiceInfo(
      file: File,
      service: String
  ): Map[String, String] = {
    val dockerId = dockerComposeDockerId(file, service)
    val ports = parseDockerPorts(service, dockerPorts(dockerId))
    val result = Map(s"$service:containerId" -> dockerId) ++ ports // TODO
    println(result)
    result
  }

  private lazy val dockerRemoveImageCommand = Command( // TODO fix
    "dockerRemoveImage",
    ("dockerRemoveImage", "Docker remove image."),
    "Supply the image name"
  )(_ => token("<image id>")) { (state: State, imageId: String) =>
    dockerRemoveImageCmd(imageId)
    state
  }
}
