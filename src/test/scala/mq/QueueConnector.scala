package mq

import com.rabbitmq.client._
import com.typesafe.config.ConfigFactory

class QueueConnector(configName: String) {
  val config = ConfigFactory.load(configName)
  val exchange = config.getString("amqp.channel.exchange")
  val exchangeType = config.getString("amqp.channel.exchangeType")
  val routingKey = config.getString("amqp.channel.routingKey")
  val queue = config.getString("amqp.channel.queue")
  val durable = config.getBoolean("amqp.channel.durable")
  val autoAck = config.getBoolean("amqp.channel.autoAck")

  val factory = new ConnectionFactory()
  factory.setUri(config.getString("amqp.url"))
  val connection = factory.newConnection()

  val channel = connection.createChannel
  channel.exchangeDeclare(exchange, exchangeType, true)
  channel.queueDeclare(queue, durable, false, false, null)
  channel.queueBind(queue, exchange, routingKey)

  def close(): Unit = connection.close()

  def push(message: String): Unit = channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())

  def pull: Option[GetResponse] = Option(channel.basicGet(queue, autoAck))

  def ack(deliveryTag: Long): this.type = {
    channel.basicAck(deliveryTag, false)
    this
  }

  def nack(deliveryTag: Long): this.type = {
    channel.basicNack(deliveryTag, false, true)
    this
  }
}