lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  name := "objektwerks.rabbitmq",
  version := "1.0",
  scalaVersion := "2.11.8",
  ivyScala := ivyScala.value map {
    _.copy(overrideScalaVersion = true)
  },
  libraryDependencies ++= {
    val akkaVersion = "2.4.8"
    Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
      "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,
      "com.rabbitmq" % "amqp-client" % "3.6.3",
      "net.ceedubs" % "ficus_2.11" % "1.1.2",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "org.scalatest" % "scalatest_2.11" % "2.2.6"
    )
  },
  scalacOptions ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-Xfatal-warnings"
  ),
  fork in test := true
)
lazy val root = (project in file(".")).
  configs(IntegrationTest).
  settings(commonSettings: _*).
  settings(Defaults.itSettings: _*).
  settings(
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6" % "it,test"
  )