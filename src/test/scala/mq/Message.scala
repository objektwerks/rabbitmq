package mq

object WorkRequest

final case class Request(message: String)

final case class Response(message: String)