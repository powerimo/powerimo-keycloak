# Keycloak to Rabbit MQ provider

## Overview

The purpose of this provider is to publish Keycloak events to a RabbitMQ server via AMQP.

## Installation

### Basics

According to the Keycloak documentation, all providers should be packaged into JAR files and copied to the "providers" directory. You can find more information [here](https://www.keycloak.org/server/configuration-provider).

### Docker "Ready-to-use" 

The easiest way to automate the integration process is to build your own Docker image. You can see an example Dockerfile [here](ready-to-use)

When the provider installed correctly you can find the following lines in Keycloak log: 
![START](/doc/html/start_provider.png)

### Docker compose

The example is [here](keycloak-dev-stack). 

## Setup

After installing the provider, you need to configure it. The settings are stored in a YAML file, which should be accessible to the provider.
By default, the path is `/etc/keycloak-mq-sender/config.yaml`. This can be changed by setting the `spi-event-listener-mq-sender-config-file` property as described in [Keycloak documentation](https://www.keycloak.org/server/configuration-provider).
For Docker, it is often more convenient to use a volume mount when starting the container.

### Configuration file

Sample configuration file:

```
enabled: true
serverId: dev
channels:
- id: channel01
  channelClassName: RabbitMqChannel
  realmName: dev
  enabled: true
  url: amqp://rabbitmq
  user: guest
  password: guest
  exchange: amq.topic
- id: channel02
  realmName: dev
  enabled: false
  url: amqp://rabbitmq
  user: guest
  password: guest
  exchange: amq.direct
```

Root properties:
- enabled - disable\enable the provider 
- serverId - the label used in the AMQP messages which allows to identify the source of events.
- channels - set of channels, other words server\exchanges where events is publishing.  

Channel properties:
- id - unique channel ID.
- channelClassName - the class used for publishing. Only value is accepted `RabbitMqChannel`.
- realmName - (default ALL_REALMS). If specified the value except ALL_REALMS only events for this realm will be published on the channel.
- url - URL of RabbitMQ server.
- user - RabbitMQ user.
- password - RabbitMQ user password.
- exchange - Exchange name for publishing.

# Publishing

## Type events

Two types events are supported: `keycloak.event` and `keycloak.admin-event` which corresponds to the Keycloak classes `Event` and `AdminEvent`, respectively. 

## Message properties

An example published message in RabbitMQ can be viewed via the Management UI (https://www.rabbitmq.com/docs/management):

![MessageHeader](/doc/html/rmq_message.png)

The following properties are used:
- Routing key - can be `keycloak.event` or `keycloak.admin-event`. It could be useful for routing. 
- Property `app_id` - always `mq-sender`.
- Header `Event-Type` - original Keycloak event class name (`Event`, `AdminEvent`).
- Header `Server-Id` - serverId property value from the configuration file.

## Payload

Example:

```
{
    "eventType": "keycloak.event",
    "event": "LOGIN",
    "serverId": "qa",
    "realmId": "d401709e-ebdd-4710-b85f-0e5cc282c38b",
    "realmName": "dev",
    "error": null,
    "userId": "7b7f293e-637b-4943-a4cd-e623e59ee9c7",
    "details": {
        "auth_method": "openid-connect",
        "redirect_uri": "http://localhost:1000/realms/dev/account/",
        "consent": "no_consent_required",
        "code_id": "2595655a-47fb-49ba-84a5-8a31f689d69a",
        "username": "user01@email.org"
    },
    "ipAddress": "192.168.112.1",
    "eventId": "411cd603-6be4-44b8-9dbb-daeabfda0300",
    "representation": null,
    "eventTime": "2024-06-16T14:47:02.287564251Z",
    "time": 1718549222287
}
```

## Java usage

The sample consumer is included in the sources as a module: [powerimo-keycloak-example-consumer](powerimo-keycloak-example-consumer).

For consumers, the easiest way to convert MQ events is to use the common library shared in Maven Central:

```
<dependency>
    <groupId>org.powerimo</groupId>
    <artifactId>powerimo-keycloak-common</artifactId>
    <version>1.0.1</version>
</dependency>
```

Sample of consume

```
import com.rabbitmq.client.*;
import org.powerimo.keycloak.MessageSerializer;
import org.powerimo.keycloak.converters.DefaultJsonSerializer;

import java.nio.charset.StandardCharsets;

public class KeycloakMqConsumer {
    private final static String QUEUE_NAME = "keycloak-events";
    private final static String HOST = "localhost";
    private final static int PORT = 5672;

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        MessageSerializer serializer = new DefaultJsonSerializer();

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(
                    String consumerTag,
                    Envelope envelope,
                    AMQP.BasicProperties properties,
                    byte[] body) {

                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("[->]: " + message + "; exchange: " + envelope.getExchange() + "; routingKey: " + envelope.getRoutingKey() + "; envelope: " + envelope);

                var event = serializer.deserializeEvent(message);
                System.out.println("[e] Received event: " + event);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
        System.out.println("* KeycloakMqConsumer started");
    }

}
```





