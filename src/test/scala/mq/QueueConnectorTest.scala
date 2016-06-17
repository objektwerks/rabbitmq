package mq

import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class QueueConnectorTest extends FunSuite with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val system = ActorSystem.create("queue", ConfigFactory.load("test.conf"))
  val broker = system.actorOf(Props[Broker], name = "broker")

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 3 seconds)
  }

  test("amqp") {
    pushMessagesToRequestQueue()
    pullMessagesFromRequestQueue()
    Thread.sleep(3000)
  }

  test("broker") {
    pushMessagesToRequestQueue()
    broker ! WorkRequest
    Thread.sleep(3000)
  }

  private def pushMessagesToRequestQueue(): Unit = {
    val requestQueue = new QueueConnector("request.queue.conf")
    val counter = new AtomicInteger()
    val confirmed = new AtomicInteger()
    for (i <- 1 to 100) {
      val message = s"test.request: ${counter.incrementAndGet}"
      val isComfirmed = requestQueue.push(message)
      if (isComfirmed) confirmed.incrementAndGet
    }
    requestQueue.close()
    assert(confirmed.intValue == 100)
  }

  private def pullMessagesFromRequestQueue(): Unit = {
    val requestQueue = new QueueConnector("request.queue.conf")
    val pulled = new AtomicInteger()
    for (i <- 1 to 100) {
      if(requestQueue.pull.nonEmpty) pulled.incrementAndGet
    }
    requestQueue.close()
    assert(pulled.intValue == 100)
  }
}