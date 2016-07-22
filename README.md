RabbitMQ
--------
>RabbitMQ feature tests using Scala and Java client API.

Install
-------
1. brew install RabbitMQ

Start
-----
1. brew services start rabbitmq

Stop
----
1. brew services stop rabbitmq

Test
----
1. sbt clean it:test

Admin
-----
>See rabbitmqadmin @ https://www.rabbitmq.com/management-cli.html

Control
-------
>See rabbitmqctl @ https://www.rabbitmq.com/man/rabbitmqctl.1.man.html

1. List Queues
   1.1 rabbitmqctl list_queues name messages_ready messages_unacknowledged
2. Restart
   2.1 rabbitmqctl stop_app
   2.2 rabbitmqctl reset
   2.3 rabbitmqctl start_app