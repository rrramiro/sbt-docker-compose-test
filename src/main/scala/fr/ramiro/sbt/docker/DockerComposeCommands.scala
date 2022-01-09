package fr.ramiro.sbt.docker

import sbt._

import scala.language.postfixOps
import scala.sys.process._

trait DockerComposeCommands {

  private def toCmd(file: File): Seq[String] =
    Seq(
      "docker-compose",
      "--file",
      file.getAbsolutePath
    )

  private def execDockerCompose(
      file: File,
      commands: String*
  ): Unit = {
    val cmd = toCmd(file) ++ commands
    println(s"run: $cmd")
    val builder = Process(cmd, file.getParentFile)
    if ((builder !) != 0) {
      throw new DockerComposeError
    }
  }

  private def getDockerCompose(
      file: File,
      commands: String*
  ): String = {
    val cmd = toCmd(file) ++ commands
    val builder = Process(cmd, file.getParentFile)
    val result = (builder !!).trim
    println(s"getDockerCompose: $cmd => $result")
    result
  }

  def dockerComposeUpCmd(
      file: File,
      envs: Map[String, String]
  ): Unit = {
    if (envs.nonEmpty) { // TODO merge
      val envFile = file.getParentFile / ".env" // TODO param docker-compose + task generate .env
      val content = envs
        .foldLeft(Seq.empty[String]) { case (acc, (key, value)) =>
          acc :+ s"$key=$value"
        }
        .mkString(System.lineSeparator())
      IO.write(envFile, content)
    }

    execDockerCompose(file, "up", "--detach")
  }

  def dockerComposeDownCmd(file: File): Unit =
    execDockerCompose(file, "down")

  def dockerComposeStopCmd(file: File): Unit =
    execDockerCompose(file, "stop")

  def dockerComposeDockerId(
      file: File,
      service: String
  ): String =
    getDockerCompose(file, "ps", "-q", service)

  def dockerComposeServices(
      file: File
  ): Seq[String] =
    getDockerCompose(file, "ps", "--services").split(Array('\n', '\r')).toSeq

  private def execDocker(dockerArgs: String*) = {
    val cmd = Seq("docker") ++ dockerArgs
    println(s"run: $cmd") // TODO logger
    cmd !
  }

  private def getDocker(dockerArgs: String*) = {
    val cmd = Seq("docker") ++ dockerArgs
    val result = (cmd !!).trim
    println(s"getDocker: $cmd => $result") // TODO logger
    result
  }

  def dockerPorts(dockerId: String): String =
    /*
    getDocker(
      "inspect",
      "--format",
      """{{range $p, $conf := .NetworkSettings.Ports}}{{$p}} -> {{(index $conf 0).HostPort}}{{end}}""",
      dockerId
    )
     */
    getDocker("port", dockerId)

  def dockerIpAddress(dockerId: String): String = getDocker(
    "inspect",
    "--format",
    """{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}""",
    dockerId
  )

  def dockerRemoveImageCmd(imageName: String): Int =
    execDocker("rmi", imageName)
}
