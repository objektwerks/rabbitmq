package mq

import akka.actor.{Actor, ActorLogging, Props}
import com.spingo.op_rabbit.RabbitControl

class Broker extends Actor with ActorLogging {
  val mq = context.actorOf(Props[RabbitControl])
  val worker = context.actorOf(Props[Worker])

  override def receive: Receive = {
    case WorkRequest =>
      // pull from mq
      worker ! Request("work")
    case response: Response => // push to mq
  }
}