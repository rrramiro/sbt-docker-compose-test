
//Set this settings when none of images in the docker-compose.yml file need to be built

lazy val root = project.in(file("."))
  .enablePlugins(PluginDockerCompose)
