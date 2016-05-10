package mq

import akka.actor.{Actor, Props}

class Broker extends Actor {
  val worker = context.actorOf(Props[Worker])
  val requestQueue = new QueueConnector("request.queue.conf")
  val responseQueue = new QueueConnector("response.queue.conf")

  override def receive: Receive = {
    case WorkRequest =>
      val request = requestQueue.pull.get
      worker ! Request(id = request.getEnvelope.getDeliveryTag, message = new String(request.getBody))
    case response: Response =>
      responseQueue.push(response.message)
      requestQueue.ack(response.id)
  }
}