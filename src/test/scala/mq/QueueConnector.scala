package mq

import com.rabbitmq.client._
import com.typesafe.config.Config

class QueueConnector(config: Config) {
  val factory = new ConnectionFactory()
  factory.setUri(config.getString("amqp.url"))
  val connection = factory.newConnection()

  val channel = connection.createChannel
  val exchange = config.getString("amqp.channel.exchange")
  val exchangeType = config.getString("amqp.channel.exchange.type")
  val routingKey = config.getString("amqp.channel.routing.key")
  val queue = config.getString("amqp.channel.queue")
  val autoAck = config.getBoolean("amqp.channel.auto.ack")
  channel.exchangeDeclare(exchange, exchangeType, true)
  channel.queueDeclare(queue, true, false, false, null)
  channel.queueBind(queue, exchange, routingKey)

  def close(): Unit = connection.close()

  def push(message: String): Unit = channel.basicPublish(exchange, routingKey, null, message.getBytes())

  def pull: GetResponse = channel.basicGet(queue, autoAck)

  def ack(deliveryTag: Long): Unit = channel.basicAck(deliveryTag, false)

  def nack(deliveryTag: Long): Unit = channel.basicNack(deliveryTag, false, true)
}