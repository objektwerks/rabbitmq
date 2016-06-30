package mq

import akka.actor.{Actor, ActorLogging, Props}

class Broker extends Actor with ActorLogging {
  val requestQueue = new QueueConnector("test.request.queue.conf")
  val responseQueue = new QueueConnector("test.response.queue.conf")
  val queue = context.actorOf(Queue.props(requestQueue, responseQueue), name = "queue")
  val worker = context.actorOf(Props[Worker], name = "worker")

  override def receive: Receive = {
    case PullRequest => queue ! PullRequest
    case request: Request => worker ! request
    case response: Response => queue ! response
    case Shutdown =>
      log.debug("broker shutting down...")
      context stop queue
      context stop worker
      context stop self
  }
}