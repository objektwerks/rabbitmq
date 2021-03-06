package mq

sealed trait Message extends Product with Serializable

case object Shutdown extends Message

case object PullRequest extends Message

final case class Request(id: Long, message: String) extends Message

final case class Response(id: Long, message: String) extends Message