lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  name := "rabbitmq",
  organization := "objektwerks",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.13.8",
  libraryDependencies ++= {
    val akkaVersion = "2.6.19"
    Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.rabbitmq" % "amqp-client" % "5.14.2",
      "com.iheart" %% "ficus" % "1.5.2",
      "com.typesafe" % "config" % "1.4.2",
      "ch.qos.logback" % "logback-classic" % "1.2.11"
    )
  }
)
lazy val root = (project in file(".")).
  configs(IntegrationTest).
  settings(commonSettings: _*).
  settings(Defaults.itSettings: _*).
  settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "it,test"
  )
