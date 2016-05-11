package mq

import akka.actor.{Actor, Props}

class Broker extends Actor {
  val worker = context.actorOf(Props[Worker])
  val requestQueue = new QueueConnector("request.queue.conf")
  val responseQueue = new QueueConnector("response.queue.conf")
  requestQueue.push("test.request")

  override def receive: Receive = {
    case WorkRequest =>
      requestQueue.pull match {
        case Some(item) =>
          worker ! Request(id = item.getEnvelope.getDeliveryTag, message = new String(item.getBody))
        case None =>
      }
    case response: Response =>
      responseQueue.push(response.message)
      requestQueue.ack(response.id)
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop: Unit = {
    requestQueue.close()
    responseQueue.close()
  }
}