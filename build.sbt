lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  name := "rabbitmq",
  organization := "objektwerks",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.13.4",
  libraryDependencies ++= {
    val akkaVersion = "2.6.11"
    Seq(
      "com.typesafe" % "config" % "1.4.0",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.rabbitmq" % "amqp-client" % "5.10.0",
      "com.iheart" %% "ficus" % "1.5.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  }
)
lazy val root = (project in file(".")).
  configs(IntegrationTest).
  settings(commonSettings: _*).
  settings(Defaults.itSettings: _*).
  settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.3" % "it,test"
  )
