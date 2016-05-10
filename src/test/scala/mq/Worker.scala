package mq

import akka.actor.Actor

class Worker extends Actor {
  override def receive: Receive = {
    case request: Request => sender ! Response(request.correlationId, "response")
  }
}