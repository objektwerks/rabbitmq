package mq

import akka.actor._
import akka.util.Timeout
import com.spingo.op_rabbit.RabbitControl
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

sealed trait Message
final case class Request(message: String) extends Message
final case class Response(message: String) extends Message

class Broker extends Actor with ActorLogging {
  val mq = context.actorOf(Props[RabbitControl])

  import context.dispatcher
  context.system.scheduler.schedule(1 second, 1 second, mq, "")

  override def receive: Receive = {
    case request: Request => log.info(s"$request")
    case response: Response => log.info(s"$response")
  }
}

class QueueTest extends FunSuite  with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val system: ActorSystem = ActorSystem.create("queue", ConfigFactory.load("test.conf"))
  val broker = system.actorOf(Props[Broker])

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
  }

  test("broker") {
  }
}