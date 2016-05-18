package mq

import com.rabbitmq.client._
import com.typesafe.config.ConfigFactory

class QueueConnector(configName: String) {
  val config = ConfigFactory.load(configName)
  val url = config.getString("amqp.factory.url")
  val exchange = config.getString("amqp.channel.exchange")
  val exchangeType = config.getString("amqp.channel.exchangeType")
  val routingKey = config.getString("amqp.channel.routingKey")
  val queue = config.getString("amqp.channel.queue")
  val durable = config.getBoolean("amqp.channel.durable")
  val autoAck = config.getBoolean("amqp.channel.autoAck")
  val publishConfirmationTimeout = config.getInt("amqp.channel.publishConfirmationTimeout")
  val connectionClosed = config.getString("amqp.message.connectionClosed")
  val publishConfirmationTimedOut = config.getString("amqp.message.publishConfirmationTimedOut")
  val connection = createConnection
  val channel = createChannel

  def push(message: String): Unit = {
    if (isConnectionAndChannelOpen) {
      channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())
      if (!channel.waitForConfirms(publishConfirmationTimeout)) throw new IllegalStateException(publishConfirmationTimedOut)
    }
  }

  def pull: Option[GetResponse] = {
    if (isConnectionAndChannelOpen) Option(channel.basicGet(queue, autoAck)) else None
  }

  def ack(deliveryTag: Long): Unit = {
    if (isConnectionAndChannelOpen) channel.basicAck(deliveryTag, false)
  }

  def nack(deliveryTag: Long): Unit = {
    if (isConnectionAndChannelOpen) channel.basicNack(deliveryTag, false, true)
  }

  def close(): Unit = {
    if (channel.isOpen) channel.close()
    if (connection.isOpen) connection.close()
  }

  private def isConnectionAndChannelOpen: Boolean = {
    if (connection.isOpen && channel.isOpen) true else throw new IllegalStateException(connectionClosed)
  }

  private def createConnection: Connection = {
    val factory = new ConnectionFactory()
    require(url.nonEmpty)
    factory.setUri(url)
    factory.newConnection()
  }

  private def createChannel: Channel = {
    if (connection.isOpen) {
      val channel = connection.createChannel
      channel.exchangeDeclare(exchange, exchangeType, true)
      channel.queueDeclare(queue, durable, false, false, null)
      channel.queueBind(queue, exchange, routingKey)
      channel.confirmSelect()
      channel
    } else throw new IllegalStateException(connectionClosed)
  }
}