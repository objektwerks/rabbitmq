package mq

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging}

class Worker extends Actor with ActorLogging {
  val counter = new AtomicInteger()

  override def receive: Receive = {
    case request: Request =>
      val message = s"test.response: ${counter.incrementAndGet}"
      log.debug(s"Worker sending Response: $message")
      sender ! Response(request.id, message)
  }
}