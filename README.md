RabbitMQ
--------
>RabbitMQ connector with feature tests using Scala and Java client API.

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
>**View** the RabbitMQ Web UI at: http://http://localhost:15672/  [ user: guest, password: guest ]

-------
>See rabbitmqctl @ https://www.rabbitmq.com/man/rabbitmqctl.1.man.html

1. List Queues
   * rabbitmqctl list_queues [ name messages_ready messages_unacknowledged ]
2. Restart
   * rabbitmqctl stop_app
   * rabbitmqctl reset
   * rabbitmqctl start_app
