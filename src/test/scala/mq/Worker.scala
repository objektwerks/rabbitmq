package mq

import akka.actor.{Actor, ActorLogging}

class Worker extends Actor with ActorLogging {
  override def receive: Receive = {
    case request: Request => sender ! Response(request.message)
  }
}