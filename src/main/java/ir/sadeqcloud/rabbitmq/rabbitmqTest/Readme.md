## spring AMQP

- AMQP 0-9-1 and AMQP 1.0 are different protocols, not different versions of the same protocol.
- Different messaging protocols use different ports .
### AMQP 0-9-1

- AMQP 0-9-1 provides a way for connections to multiplex over a single TCP connection.That means an application can open multiple "lightweight connections" called channels on a single connection. AMQP 0-9-1 clients open one or more channels after connecting and perform protocol operations (manage topology, publish, consume) on the channels.
- AMQP 0-9-1 has a model that includes connections and channels. Channels allow for connection multiplexing (having multiple logical connections on a "physical" or TCP one).
### AMQP 1.0

- AMQP 1.0 provides a way for connections to multiplex over a single TCP connection. That means an application can open multiple "lightweight connections" called sessions on a single connection. Applications then set up one or more links to publish and consume messages.
