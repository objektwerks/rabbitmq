package mq

import akka.actor.{Actor, Props}

class Broker extends Actor {
  val worker = context.actorOf(Props[Worker])

  override def receive: Receive = {
    case WorkRequest =>
      // pull from request queue
      worker ! Request(message = "request")
    case response: Response => // push to response queue
  }
}