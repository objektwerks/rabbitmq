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

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

class TestQueueConsumer(connector: QueueConnector) extends QueueConsumer(connector) {
  val log = LoggerFactory.getLogger(this.getClass)

  override def handleDelivery(consumerTag: String,
                              envelope: Envelope,
                              properties: BasicProperties,
                              body: Array[Byte]): Unit = {
    val message = new String(body, StandardCharsets.UTF_8)
    log.debug(s"handleDeliver: $message")
    connector.ackAllMessages(envelope.getDeliveryTag)
  }
}

class QueueConnectorTest extends AnyFunSuite with BeforeAndAfterAll {
  val log = LoggerFactory.getLogger(this.getClass)
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
    log.debug("push pull test: test rabbitmq queue cleared!")
    pushMessagesToRequestQueue(queue, 10)
    pullMessagesFromRequestQueue(queue, 10)
    queue.close()
  }

  test("consume") {
    val queueConf = ConfigFactory.load("test.queue.conf").as[QueueConnectorConf]("queue")
    val queue = new QueueConnector(queueConf)
    clearQueue(queue)
    log.debug("consume test: test rabbitmq queue cleared!")
    pushMessagesToRequestQueue(queue, 10)
    val consumer = new TestQueueConsumer(queue)
    consumeMessagesFromRequestQueue(queue, 10, consumer)
    queue.close()
  }

  test("broker") {
    val requestQueue = new QueueConnector(ConfigFactory.load("test.request.queue.conf").as[QueueConnectorConf]("queue"))
    clearQueue(requestQueue)
    log.debug("broker test: request queue cleared!")
    pushMessagesToRequestQueue(requestQueue, 10)
    requestQueue.close()
    broker ! PullRequest
    Thread.sleep(1000)
    val responseQueue = new QueueConnector(ConfigFactory.load("test.response.queue.conf").as[QueueConnectorConf]("queue"))
    clearQueue(responseQueue)
    log.debug("broker test: response queue cleared!")
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
    assert(confirmed.intValue == number)
  }

  private def pullMessagesFromRequestQueue(queue: QueueConnector, number: Int): Unit = {
    val pulled = new AtomicInteger()
    for (i <- 1 to number) {
      if(queue.pull.nonEmpty) pulled.incrementAndGet
    }
    assert(pulled.intValue == number)
  }

  private def consumeMessagesFromRequestQueue(queue: QueueConnector, number: Int, consumer: QueueConsumer): Unit = {
    val consumed = queue.consume(number, consumer)
    log.debug(s"consumer: $consumed")
    assert(queue.pull.isEmpty)
  }

  private def clearQueue(queue: QueueConnector): Unit = {
    var queueIsEmpty = false
    while (!queueIsEmpty) {
      queueIsEmpty = queue.pull.isEmpty
    }
  }
}