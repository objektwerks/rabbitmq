package mq

sealed trait Message
object WorkRequest extends Message
final case class Request(message: String) extends Message
final case class Response(message: String) extends Message