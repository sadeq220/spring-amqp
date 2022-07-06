## spring AMQP

- AMQP 0-9-1 and AMQP 1.0 are different protocols, not different versions of the same protocol.
- Different messaging protocols use different ports .
### AMQP 0-9-1

- AMQP 0-9-1 provides a way for connections to multiplex over a single TCP connection.That means an application can open multiple "lightweight connections" called channels on a single connection. AMQP 0-9-1 clients open one or more channels after connecting and perform protocol operations (manage topology, publish, consume) on the channels.
- AMQP 0-9-1 has a model that includes connections and channels. Channels allow for connection multiplexing (having multiple logical connections on a "physical" or TCP one).
- AMQP 0-9-1 connections are multiplexed with channels that can be thought of as "lightweight connections that share a single TCP connection".
- Every protocol operation performed by a client happens on a channel. Communication on a particular channel is completely separate from communication on another channel, therefore every protocol method also carries a channel ID.
### AMQP 1.0

- AMQP 1.0 provides a way for connections to multiplex over a single TCP connection. That means an application can open multiple "lightweight connections" called sessions on a single connection. Applications then set up one or more links to publish and consume messages.


## AMQP acknowledgement mode
NONE => No acks - autoAck=true in Channel.basicConsume() . (means message will be deleted from queue even when exception raises )
AUTO => default - Auto - the container will issue the ack/nack based on whether the listener returns normally, or throws an exception.
MANUAL => Manual acks - user must ack/nack via a channel aware listener.