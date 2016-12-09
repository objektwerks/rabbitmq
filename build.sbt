lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  name := "rabbitmq",
  organization := "objektwerks",
  version := "1.0",
  scalaVersion := "2.12.1",
  ivyScala := ivyScala.value map {
    _.copy(overrideScalaVersion = true)
  },
  libraryDependencies ++= {
    val akkaVersion = "2.4.14"
    Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.akka" % "akka-actor_2.12" % akkaVersion,
      "com.typesafe.akka" % "akka-slf4j_2.12" % akkaVersion,
      "com.rabbitmq" % "amqp-client" % "4.0.0",
      "com.iheart" % "ficus_2.12" % "1.4.0",
      "ch.qos.logback" % "logback-classic" % "1.1.7"
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
    libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.1" % "it,test"
  )