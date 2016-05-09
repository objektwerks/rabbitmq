name := "objektwerks.rabbitmq"
version := "1.0"
scalaVersion := "2.11.8"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
resolvers ++= Seq(
  "SpinGo OSS" at "http://spingo-oss.s3.amazonaws.com/repositories/releases"
)
libraryDependencies ++= {
  val akkaVersion = "2.4.4"
  val opRabbitVersion = "1.3.0"
  Seq(
    "com.typesafe" % "config" % "1.3.0",
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.spingo" % "op-rabbit-core_2.11" % opRabbitVersion,
    "com.spingo" % "op-rabbit-play-json_2.11" % opRabbitVersion,
    "com.spingo" % "op-rabbit-json4s_2.11" % opRabbitVersion,
    "com.spingo" % "op-rabbit-airbrake_2.11" % opRabbitVersion,
    "com.spingo" % "op-rabbit-akka-stream_2.11" % opRabbitVersion,
    "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
  )
}
scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Xfatal-warnings"
)
fork in test := true
javaOptions += "-server -Xss1m -Xmx2g"
logLevel := Level.Info