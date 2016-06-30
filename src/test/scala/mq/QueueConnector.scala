package mq

import com.rabbitmq.client._
import com.typesafe.config.ConfigFactory

private[this] class Connector(val connection: Connection, val channel: Channel) {
  def isOpen: Boolean = if (connection.isOpen && channel.isOpen) true else false
  def close(): Unit = if (connection.isOpen) connection.close()
}

class QueueConnector(configName: String) {
  private val config = ConfigFactory.load(configName)
  private val url = config.getString("amqp.connection.url")
  private val exchange = config.getString("amqp.channel.exchange")
  private val exchangeType = config.getString("amqp.channel.exchangeType")
  private val routingKey = config.getString("amqp.channel.routingKey")
  private val queue = config.getString("amqp.channel.queue")
  private val durable = config.getBoolean("amqp.channel.durable")
  private val autoAck = config.getBoolean("amqp.channel.autoAck")
  private val publishConfirmationTimeout = config.getInt("amqp.channel.publishConfirmationTimeout")
  private var connector = connect()

  def pull: Option[GetResponse] = {
    checkConnector()
    Option(connector.channel.basicGet(queue, autoAck))
  }

  def push(message: String): Boolean = {
    checkConnector()
    connector.channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())
    connector.channel.waitForConfirms(publishConfirmationTimeout)
  }

  def ack(deliveryTag: Long): Unit = {
    checkConnector()
    connector.channel.basicAck(deliveryTag, false)
  }

  def nack(deliveryTag: Long): Unit = {
    checkConnector()
    connector.channel.basicNack(deliveryTag, false, true)
  }

  def close(): Unit = connector.close()

  private def connect(): Connector = {
    if (connector != null) connector.close()
    val connection = createConnection()
    val channel = createChannel(connection)
    new Connector(connection, channel)
  }

  private def checkConnector(): Unit = if (!connector.isOpen) connector = connect()

  private def createConnection(): Connection = {
    val factory = new ConnectionFactory()
    factory.setUri(url)
    factory.newConnection()
  }

  private def createChannel(connection: Connection): Channel = {
    val channel = connection.createChannel
    channel.exchangeDeclare(exchange, exchangeType, true)
    channel.queueDeclare(queue, durable, false, false, null)
    channel.queueBind(queue, exchange, routingKey)
    channel.confirmSelect()
    channel
  }
}