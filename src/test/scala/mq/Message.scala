package mq

sealed trait Message
case object WorkRequest extends Message
final case class Request(id: Long, message: String) extends Message
final case class Response(id: Long, message: String) extends Message