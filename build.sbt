lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  name := "rabbitmq",
  organization := "objektwerks",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.13.6",
  libraryDependencies ++= {
    val akkaVersion = "2.6.15"
    Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.rabbitmq" % "amqp-client" % "5.13.0",
      "com.iheart" %% "ficus" % "1.5.0",
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.5"
    )
  }
)
lazy val root = (project in file(".")).
  configs(IntegrationTest).
  settings(commonSettings: _*).
  settings(Defaults.itSettings: _*).
  settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "it,test"
  )
