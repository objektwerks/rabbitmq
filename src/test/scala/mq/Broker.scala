package mq

import akka.actor.{Actor, ActorLogging, Props}

class Broker extends Actor with ActorLogging {
  val worker = context.actorOf(Props[Worker], name = "worker")
  val requestQueue = new QueueConnector("test.request.queue.conf")
  val responseQueue = new QueueConnector("test.response.queue.conf")

  override def receive: Receive = {
    case WorkRequest =>
      requestQueue.pull match {
        case Some(item) =>
          val id = item.getEnvelope.getDeliveryTag
          val message = new String(item.getBody)
          log.debug(s"Broker sending queue request: $id : $message")
          worker ! Request(id, message)
        case None =>
          log.debug(s"Broker processed all requests / responses, and is shutting down...")
          require(requestQueue.pull.isEmpty)
          context stop worker
          context stop self
      }
    case Response(id, message) =>
      log.debug(s"Broker receiving worker response: $id - $message")
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