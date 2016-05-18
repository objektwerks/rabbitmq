package mq

import akka.actor.{Actor, ActorLogging, Props}

class Broker extends Actor with ActorLogging {
  val worker = context.actorOf(Props[Worker], name = "worker")
  val requestQueue = new QueueConnector("request.queue.conf")
  val responseQueue = new QueueConnector("response.queue.conf")

  override def receive: Receive = {
    case WorkRequest =>
      requestQueue.pull match {
        case Some(item) =>
          val id = item.getEnvelope.getDeliveryTag
          val message = new String(item.getBody)
          log.debug(s"Broker receiving Request: $id - $message")
          worker ! Request(id, message)
        case None =>
          log.debug(s"Broker processed all requests, and is shutting down...")
          require(requestQueue.pull.isEmpty)
          context stop worker
          context stop self
      }
    case Response(id, message) =>
      log.debug(s"Broker receiving Response: $id - $message")
      val wasDelivered = responseQueue.push(message)
      if (wasDelivered) requestQueue.ack(id)
      self ! WorkRequest
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop: Unit = {
    requestQueue.close()
    responseQueue.close()
  }
}