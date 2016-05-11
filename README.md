RabbitMQ
--------
>RabbitMQ feature tests using Scala.

Install
-------
1. brew install RabbitMQ
2. brew services start rabbitmq

Test
----
1. sbt clean test

Admin
-----
1. rabbitmqadmin - https://www.rabbitmq.com/management-cli.html
2. rabbitmqctl - https://www.rabbitmq.com/man/rabbitmqctl.1.man.html

Commands
--------
1. List Queues
   1. rabbitmqctl list_queues name messages_ready messages_unacknowledged
2. Restart
   1. rabbitmqctl stop_app
   2. rabbitmqctl reset
   3. rabbitmqctl start_app
