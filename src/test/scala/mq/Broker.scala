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
          val message = new String(item.getBody, "UTF-8")
          log.debug("request: {}", id)
          worker ! Request(id, message)
        case None =>
          log.debug("broker shutting down...")
          require(requestQueue.pull.isEmpty)
          context stop worker
          context stop self
      }
    case Response(id, message) =>
      log.debug("response: {}", id)
      val wasPushed = responseQueue.push(message)
      require(wasPushed)
      requestQueue.ack(id)
      self ! WorkRequest
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop: Unit = {
    requestQueue.close()
    responseQueue.close()
  }
}