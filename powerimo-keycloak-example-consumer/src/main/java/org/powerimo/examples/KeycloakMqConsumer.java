package org.powerimo.examples;

import com.rabbitmq.client.*;
import org.powerimo.keycloak.KcEvent;
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
