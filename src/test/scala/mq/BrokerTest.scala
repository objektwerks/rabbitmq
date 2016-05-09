package mq

import akka.actor._
import akka.util.Timeout
import com.spingo.op_rabbit.RabbitControl
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class BrokerTest extends FunSuite  with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val system: ActorSystem = ActorSystem.create("queue", ConfigFactory.load("test.conf"))
  val queue = system.actorOf(Props[RabbitControl])
  val worker = system.actorOf(Props[Worker])
  val broker = system.actorOf(Props(new Broker(queue, worker)))

  override protected def beforeAll(): Unit = {

  }

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
  }

  test("broker") {
    broker ! WorkRequest
  }
}