package mq

import akka.actor.{Actor, ActorLogging}

class Worker extends Actor with ActorLogging {
  override def receive: Receive = {
    case Request(id, message) =>
      log.debug(s"Worker sending response: $id : $message")
      sender ! Response(id, message)
  }
}