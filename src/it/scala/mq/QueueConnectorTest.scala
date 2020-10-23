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
import scala.language.postfixOps

class TestQueueConsumer(connector: QueueConnector) extends QueueConsumer {
  val logger = LoggerFactory.getLogger(getClass)

  override def handleDelivery(consumerTag: String,
                              envelope: Envelope,
                              properties: BasicProperties,
                              body: Array[Byte]): Unit = {
    val message = new String(body, StandardCharsets.UTF_8)
    logger.info(s"*** handleDelivery: $message")
    connector.ackAllMessages(envelope.getDeliveryTag)
  }
}

class QueueConnectorTest extends AnyFunSuite with BeforeAndAfterAll {
  val logger = LoggerFactory.getLogger(getClass)
  implicit val timeout = Timeout(1 second)

  val system = ActorSystem.create("queue", ConfigFactory.load("test.akka.conf"))
  val broker = system.actorOf(Props[Broker](), name = "broker")
  val queue = new QueueConnector( ConfigFactory.load("test.queue.conf").as[QueueConnectorConf]("queue") )
  val consumer = new TestQueueConsumer(queue)

  override protected def afterAll(): Unit = {
    queue.close()
    Await.result(system.terminate(), 3 seconds)
    ()
  }

  test("push pull") {
    pushMessagesToRequestQueue(queue, 10)
    pullMessagesFromRequestQueue(queue, 10)
  }

  test("consume") {
    pushMessagesToRequestQueue(queue, 10)
    consumeMessagesFromRequestQueue(queue, 10, consumer)
  }

  test("broker") {
    val requestQueue = new QueueConnector(ConfigFactory.load("test.request.queue.conf").as[QueueConnectorConf]("queue"))
    pushMessagesToRequestQueue(requestQueue, 10)
    requestQueue.close()

    broker ! PullRequest
    Thread.sleep(1000)

    val responseQueue = new QueueConnector(ConfigFactory.load("test.response.queue.conf").as[QueueConnectorConf]("queue"))
    clearQueue(responseQueue)
    responseQueue.close()
  }

  private def pushMessagesToRequestQueue(queue: QueueConnector, count: Int): Unit = {
    val counter = new AtomicInteger()
    val confirmed = new AtomicInteger()
    for (_ <- 1 to count) {
      val message = s"message [${counter.incrementAndGet}]"
      val isComfirmed = queue.push(message)
      if (isComfirmed) confirmed.incrementAndGet
    }
    assert(confirmed.intValue == count)
    ()
  }

  private def pullMessagesFromRequestQueue(queue: QueueConnector, count: Int): Unit = {
    val pulled = new AtomicInteger()
    for (_ <- 1 to count) {
      if(queue.pull.nonEmpty) pulled.incrementAndGet
    }
    assert(pulled.intValue == count)
    ()
  }

  private def consumeMessagesFromRequestQueue(queue: QueueConnector, count: Int, consumer: QueueConsumer): Unit = {
    val consumed = queue.consume(count, consumer)
    assert(consumed.nonEmpty)
    assert(queue.pull.isEmpty)
    ()
  }

  private def clearQueue(queue: QueueConnector): Unit = {
    var queueIsEmpty = false
    while (!queueIsEmpty) {
      queueIsEmpty = queue.pull.isEmpty
    }
    logger.info("*** Queue cleared!")
  }
}