package mq

import java.util.UUID

object WorkRequest

case class Request(correlationId: String = UUID.randomUUID.toString, message: String)

case class Response(correlationId: String, message: String)