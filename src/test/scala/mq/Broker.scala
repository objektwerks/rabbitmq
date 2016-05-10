package mq

import akka.actor.{Actor, Props}

class Broker(requestQueue: QueueConnector, responseQueue: QueueConnector) extends Actor {
  val worker = context.actorOf(Props[Worker])

  override def receive: Receive = {
    case WorkRequest =>
      val request = requestQueue.pull.get
      worker ! Request(id = request.getEnvelope.getDeliveryTag, message = new String(request.getBody))
    case response: Response =>
      requestQueue.ack(response.id)
      responseQueue.push(response.message)
  }
}