package mq

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

import akka.actor._
import akka.util.Timeout
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Envelope
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class TestQueueConsumer(connector: QueueConnector) extends QueueConsumer(connector) {
  override def handleDelivery(consumerTag: String,
                              envelope: Envelope,
                              properties: BasicProperties,
                              body: Array[Byte]): Unit = {
    val message = new String(body, StandardCharsets.UTF_8)
    println(s"handleDeliver: $message")
    connector.ackAllMessages(envelope.getDeliveryTag)
  }
}

class QueueConnectorTest extends FunSuite with BeforeAndAfterAll {
  implicit val timeout = Timeout(1 second)

  val system = ActorSystem.create("queue", ConfigFactory.load("test.akka.conf"))
  val broker = system.actorOf(Props[Broker], name = "broker")

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 3 seconds)
  }

  test("push pull") {
    val queueConf = ConfigFactory.load("test.queue.conf").as[QueueConnectorConf]("queue")
    val queue = new QueueConnector(queueConf)
    clearQueue(queue)
    println("push pull test: test rabbitmq queue cleared!")
    pushMessagesToRequestQueue(queue, 10)
    pullMessagesFromRequestQueue(queue, 10)
    queue.close()
  }

  test("consume") {
    val queueConf = ConfigFactory.load("test.queue.conf").as[QueueConnectorConf]("queue")
    val queue = new QueueConnector(queueConf)
    clearQueue(queue)
    println("consume test: test rabbitmq queue cleared!")
    pushMessagesToRequestQueue(queue, 10)
    consumeMessagesFromRequestQueue(queue, 10)
    queue.close()
  }

  test("broker") {
    val requestQueue = new QueueConnector(ConfigFactory.load("test.request.queue.conf").as[QueueConnectorConf]("queue"))
    clearQueue(requestQueue)
    println("broker test: request queue cleared!")
    pushMessagesToRequestQueue(requestQueue, 10)
    requestQueue.close()
    broker ! PullRequest
    Thread.sleep(1000)
    val responseQueue = new QueueConnector(ConfigFactory.load("test.response.queue.conf").as[QueueConnectorConf]("queue"))
    clearQueue(responseQueue)
    println("broker test: response queue cleared!")
    responseQueue.close()
  }

  private def pushMessagesToRequestQueue(queue: QueueConnector, number: Int): Unit = {
    val counter = new AtomicInteger()
    val confirmed = new AtomicInteger()
    for (i <- 1 to number) {
      val message = s"test.request: ${counter.incrementAndGet}"
      val isComfirmed = queue.push(message)
      if (isComfirmed) confirmed.incrementAndGet
    }
    queue.close()
    assert(confirmed.intValue == number)
  }

  private def pullMessagesFromRequestQueue(queue: QueueConnector, number: Int): Unit = {
    val pulled = new AtomicInteger()
    for (i <- 1 to number) {
      if(queue.pull.nonEmpty) pulled.incrementAndGet
    }
    queue.close()
    assert(pulled.intValue == number)
  }

  private def consumeMessagesFromRequestQueue(queue: QueueConnector, number: Int): Unit = {
    val consumer = new TestQueueConsumer(queue)
    queue.consume(number, consumer)
    Thread.sleep(1000)
    assert(queue.pull.isEmpty)
    queue.close()
  }

  private def clearQueue(queue: QueueConnector): Unit = {
    var queueIsEmpty = false
    while (!queueIsEmpty) {
      queueIsEmpty = queue.pull.isEmpty
    }
  }
}