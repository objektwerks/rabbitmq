package mq

import akka.actor.{Actor, ActorRef}

class Broker(queue: ActorRef, worker: ActorRef) extends Actor {
  override def receive: Receive = {
    case WorkRequest =>
      // pull from request queue
      worker ! Request("work")
    case response: Response => // push to response queue
  }
}