addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.8.0")

sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("fr.ramiro" % "sbt-docker-compose"  % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}