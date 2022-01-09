
lazy val basic = project.in(file("."))
  .enablePlugins(JavaAppPackaging, PluginDockerCompose)
  .settings(
    name := "basic",
    version := "1.0.0",
    scalaVersion := "2.12.15",
    dockerComposeUp := {
        (dockerComposeUp dependsOn (Docker / publishLocal)).value
    },
    dockerEnvironmentVars := Map("SOURCE_PORT" -> "5555")
  )
