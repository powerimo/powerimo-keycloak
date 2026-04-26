# Keycloak to RabbitMQ Provider

## Overview

A Keycloak SPI event listener that publishes authentication and administration events to a RabbitMQ server via AMQP. Both regular Keycloak events (`LOGIN`, `REGISTER`, etc.) and admin events (`CREATE`, `DELETE`, etc.) are supported. Events are serialized as JSON and sent to configurable exchanges with realm-level filtering.

**Supported Keycloak version:** 24.x  
**Java:** 17+

## How It Works

```
Keycloak event/admin-event
        │
   KcListener (SPI: mq-sender)
        │
   HostChannel  ──fan-out──►  RabbitMqChannel (channel01)
                          └──►  RabbitMqChannel (channel02)
```

Each channel independently filters events by realm and publishes to its own RabbitMQ exchange. A channel error never interrupts other channels.

## Installation

### Direct JAR Installation

According to the [Keycloak documentation](https://www.keycloak.org/server/configuration-provider), providers must be placed in the `providers/` directory and Keycloak must be rebuilt:

```bash
cp powerimo-keycloak-provider-<version>.jar /opt/keycloak/providers/
/opt/keycloak/bin/kc.sh build
```

Pre-built provider JARs are available on the [Releases](https://github.com/powerimo/powerimo-keycloak/releases) page.

### Docker — Ready-to-Use Image

The `ready-to-use/` directory contains a Dockerfile that builds a Keycloak image with the provider pre-installed. It downloads the release ZIP from GitHub and copies the JARs into the image automatically:

```bash
cd ready-to-use
docker build -t rtu-keycloak .
docker run --name=rtu-keycloak-instance \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v $(pwd)/mq-config:/etc/keycloak-mq-sender \
  -p 1000:8080 \
  rtu-keycloak start-dev
```

### Docker Compose (Dev Stack)

The `keycloak-dev-stack/` directory contains a Docker Compose file that starts Keycloak and RabbitMQ together. First build the `local-keycloak` image from `ready-to-use/`:

```bash
# 1. Build the image
cd ready-to-use && docker build -t local-keycloak . && cd ..

# 2. Start the stack (Keycloak on :1000, RabbitMQ on :5672, management UI on :15672)
cd keycloak-dev-stack && docker compose up
```

When the provider loads correctly, the Keycloak log will contain lines like:

```
mq-sender initialized...
mq-sender initialization complete.
mq-sender factory started
```

## Setup

The provider reads its settings from a YAML file. The default path is `/etc/keycloak-mq-sender/config.yaml`. To use a different path, set the Keycloak property `spi-event-listener-mq-sender-config-file`:

```
--spi-event-listener-mq-sender-config-file=/path/to/config.yaml
```

For Docker, mount the config directory as a volume (see examples above).

After starting Keycloak, activate the listener in the admin console: **Realm Settings → Events → Event listeners → add `mq-sender`**.

### Configuration File

```yaml
enabled: true
serverId: production
channels:
  - id: channel01
    channelClassName: RabbitMqChannel
    realmName: my-realm
    enabled: true
    url: amqp://rabbitmq
    user: guest
    password: guest
    exchange: amq.topic
  - id: channel02
    channelClassName: RabbitMqChannel
    realmName: ALL_REALMS
    enabled: true
    url: amqp://user:password@rabbitmq.internal:5672
    user: keycloak
    password: secret
    exchange: amq.fanout
```

### Root Properties

| Property | Description |
|---|---|
| `enabled` | Enable or disable the provider entirely |
| `serverId` | Arbitrary label added to every message (identifies the source server) |
| `channels` | List of channel configurations |

### Channel Properties

| Property | Required | Default | Description |
|---|---|---|---|
| `id` | yes | — | Unique channel identifier |
| `channelClassName` | no | `RabbitMqChannel` | Publishing class. Only `RabbitMqChannel` is currently supported |
| `realmName` | no | `ALL_REALMS` | Realm filter. Use `ALL_REALMS` or omit to publish events from all realms |
| `enabled` | yes | — | Enable or disable this channel |
| `url` | yes | — | RabbitMQ connection URL (`amqp://host` or `amqp://user:pass@host:port`) |
| `user` | yes | — | RabbitMQ username |
| `password` | yes | — | RabbitMQ password |
| `exchange` | yes | — | Target RabbitMQ exchange name |

## Publishing

### Routing Keys

| Event origin | Routing key |
|---|---|
| Regular event (`LOGIN`, `REGISTER`, …) | `keycloak.event` |
| Admin event (`CREATE`, `DELETE`, …) | `keycloak.admin-event` |

### Message Properties

| Property | Value |
|---|---|
| `app_id` | `mq-sender` |
| `content-type` | `application/json` |
| `content-encoding` | `UTF-8` |
| Header `Event-Type` | Keycloak Java class name (`Event` or `AdminEvent`) |
| Header `Server-Id` | `serverId` from the configuration file |
| Header `__TypeId__` | Fully-qualified Java class name of the payload (`org.powerimo.keycloak.KcEvent`) — used by Spring AMQP for automatic deserialization |

### Message Payload

All events are serialized as a single unified `KcEvent` JSON object:

```json
{
    "eventType": "keycloak.event | keycloak.admin-event",
    "event":     "LOGIN | REGISTER | CREATE | DELETE | ...",
    "serverId":  "production",
    "realmId":   "d401709e-ebdd-4710-b85f-0e5cc282c38b",
    "realmName": "my-realm",
    "error":     null,
    "userId":    "7b7f293e-637b-4943-a4cd-e623e59ee9c7",
    "details":   { "key": "value" },
    "ipAddress": "192.168.1.1",
    "eventId":   "411cd603-6be4-44b8-9dbb-daeabfda0300",
    "representation": null,
    "eventTime": "2024-06-16T14:47:02.287564251Z",
    "time":      1718549222287
}
```

#### Example: LOGIN

```json
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

#### Example: REGISTER (via Google)

```json
{
    "eventType": "keycloak.event",
    "event": "REGISTER",
    "serverId": "qa",
    "realmId": "d401709e-ebdd-4710-b85f-0e5cc282c38b",
    "realmName": "dev",
    "error": null,
    "userId": "765257b7-4e1c-4c27-aa3a-74bb90283a51",
    "details": {
        "identity_provider": "google",
        "register_method": "broker",
        "identity_provider_identity": "sportardor@gmail.com",
        "code_id": "01b161d8-c036-4f8e-89e5-db13b63e3171",
        "email": "someuser@gmail.com",
        "username": "someuser@gmail.com"
    },
    "ipAddress": "1.1.1.1",
    "eventId": "d3702ece-bce1-49a3-ac8d-386735597ae3",
    "representation": null,
    "eventTime": "2024-06-24T05:52:54.094138862Z",
    "time": 1719208374093
}
```

#### Example: Admin event — CREATE user

```json
{
    "eventType": "keycloak.admin-event",
    "event": "CREATE",
    "serverId": "qa",
    "realmId": "d401709e-ebdd-4710-b85f-0e5cc282c38b",
    "realmName": "dev",
    "error": null,
    "userId": "b2de97ee-2771-494c-af86-c0da53182181",
    "details": {
        "realmId": "d401709e-ebdd-4710-b85f-0e5cc282c38b",
        "user_id": "b2de97ee-2771-494c-af86-c0da53182181",
        "ip_address": "1.1.1.1",
        "client_id": "b27587e9-6184-4b18-9a06-6edd75c99089"
    },
    "ipAddress": "1.1.1.1",
    "eventId": "1e3f8093-838d-4259-af05-8e9f4a82a6a5",
    "representation": "{\"firstName\":\"user01@email.org\",\"lastName\":\"user01@email.org\",\"email\":\"user01@email.org\",\"emailVerified\":true}",
    "eventTime": "2024-06-24T12:17:47.458879684Z",
    "time": 1719231467458
}
```

#### Example: Admin event — DELETE user

```json
{
    "eventType": "keycloak.admin-event",
    "event": "DELETE",
    "serverId": "qa",
    "realmId": "d401709e-ebdd-4710-b85f-0e5cc282c38b",
    "realmName": "dev",
    "error": null,
    "userId": "b2de97ee-2771-494c-af86-c0da53182181",
    "details": {
        "realmId": "d401709e-ebdd-4710-b85f-0e5cc282c38b",
        "user_id": "b2de97ee-2771-494c-af86-c0da53182181",
        "ip_address": "172.20.0.1",
        "client_id": "b27587e9-6184-4b18-9a06-6edd75c99089"
    },
    "ipAddress": "172.20.0.1",
    "eventId": "28b893fd-8524-4d8c-82cb-c907254f6f48",
    "representation": null,
    "eventTime": "2024-06-24T12:18:54.131073660Z",
    "time": 1719231534130
}
```

## Java Consumer

The `powerimo-keycloak-common` library provides `KcEvent` and `DefaultJsonSerializer` to make consuming messages straightforward. It is published to Maven Central:

```xml
<dependency>
    <groupId>org.powerimo</groupId>
    <artifactId>powerimo-keycloak-common</artifactId>
    <version>1.2.1</version>
</dependency>
```

A complete working consumer example is included in the `powerimo-keycloak-example-consumer` module.

### Minimal Consumer Example

```java
import com.rabbitmq.client.*;
import org.powerimo.keycloak.MessageSerializer;
import org.powerimo.keycloak.converters.DefaultJsonSerializer;

import java.nio.charset.StandardCharsets;

public class KeycloakMqConsumer {
    private static final String QUEUE_NAME = "keycloak-events";
    private static final String HOST = "localhost";
    private static final int PORT = 5672;

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        MessageSerializer serializer = new DefaultJsonSerializer();

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body, StandardCharsets.UTF_8);
                var event = serializer.deserializeEvent(message);
                System.out.println("Received: " + event);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
        System.out.println("KeycloakMqConsumer started");
    }
}
```

### Spring AMQP

The `__TypeId__` header is set to `org.powerimo.keycloak.KcEvent`, so Spring AMQP can deserialize messages automatically when using `Jackson2JsonMessageConverter`:

```java
@RabbitListener(queues = "keycloak-events")
public void handleEvent(KcEvent event) {
    // event is already deserialized
}
```

## Building from Source

```bash
# Build and test all modules
mvn clean package

# Build with an explicit version
mvn clean package -Drevision=1.2.0
```

The provider artifact is `powerimo-keycloak-provider/target/powerimo-keycloak-provider-<version>.jar`. It is a shaded (uber) JAR that includes `amqp-client` but excludes Keycloak and Jackson libraries, which are provided by the Keycloak runtime.

## License

Apache License 2.0
