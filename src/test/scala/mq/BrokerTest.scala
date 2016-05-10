package mq

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class BrokerTest extends FunSuite  with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val system: ActorSystem = ActorSystem.create("queue", ConfigFactory.load("test.conf"))
  val broker = system.actorOf(Props[Broker])
  val requestQueue = new QueueConnector("request.queue.conf")
  val responseQueue = new QueueConnector("response.queue.conf")

  override protected def afterAll(): Unit = {
    requestQueue.close()
    responseQueue.close()
    Await.result(system.terminate(), 1 second)
  }

  test("broker") {
    requestQueue.push("test.request")
    broker ! WorkRequest
    // assert(requestQueue.pull.isEmpty)
    assert(responseQueue.pull.nonEmpty)
  }
}