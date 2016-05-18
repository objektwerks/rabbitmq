package mq

import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class BrokerTest extends FunSuite with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)
  val system = ActorSystem.create("queue", ConfigFactory.load("test.conf"))
  val broker = system.actorOf(Props[Broker], name = "broker")

  override protected def beforeAll(): Unit = {
    val requestQueue = new QueueConnector("request.queue.conf")
    val counter = new AtomicInteger()
    val delivered = new AtomicInteger()
    for (i <- 1 to 100) {
      val message = s"test.request: ${counter.incrementAndGet}"
      val wasDelivered = requestQueue.push(message)
      if (wasDelivered) delivered.incrementAndGet()
    }
    requestQueue.close()
    assert(delivered.intValue() == 100)
  }

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 3 seconds)
  }

  test("broker") {
    broker ! WorkRequest
    Thread.sleep(3000)
  }
}