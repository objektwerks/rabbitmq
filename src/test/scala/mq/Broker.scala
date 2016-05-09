package mq

import akka.actor.{Actor, ActorRef}

object WorkRequest
final case class Request(message: String)
final case class Response(message: String)

class Broker(worker: ActorRef) extends Actor {
  override def receive: Receive = {
    case WorkRequest =>
      // pull from request queue
      worker ! Request("work")
    case response: Response => // push to response queue
  }
}