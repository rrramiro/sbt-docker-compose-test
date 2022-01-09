package fr.ramiro.sbt.docker

import fr.ramiro.sbt.docker.PluginDockerCompose
import org.scalatest.funsuite.AnyFunSuite

class DockerComposeCommandsTest extends AnyFunSuite {
  test("parse one line") {
    val (key, value) = PluginDockerCompose.parseDockerPorts("basic", "5005/tcp -> 0.0.0.0:32781").head
    assert(key === "basic:5005")
    assert(value === "0.0.0.0:32781")
  }

  test("parse multiple line") {
    val input =
      """5005/tcp -> 0.0.0.0:49169
        |5005/tcp -> :::49169
        |8080/tcp -> 0.0.0.0:49168
        |8080/tcp -> :::49168""".stripMargin
    val result = PluginDockerCompose.parseDockerPorts("basic", input)
    assert(result("basic:5005") === "0.0.0.0:49169")
    assert(result("basic:8080") === "0.0.0.0:49168")
  }
}
