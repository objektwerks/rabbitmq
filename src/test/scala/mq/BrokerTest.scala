package mq

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class BrokerTest extends FunSuite  with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val requestQueue = new QueueConnector("request.queue.conf")
  val responseQueue = new QueueConnector("response.queue.conf")
  val system: ActorSystem = ActorSystem.create("queue", ConfigFactory.load("test.conf"))
  val broker = system.actorOf(Props(new Broker(requestQueue, responseQueue)))

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
    requestQueue.close()
    responseQueue.close()
  }

  test("broker") {
    requestQueue.push("test.request")
    broker ! WorkRequest
    assert(requestQueue.pull.isEmpty)
    assert(responseQueue.pull.nonEmpty)
  }
}