package mq

object WorkRequest

case class Request(id: Long, message: String)

case class Response(id: Long, message: String)