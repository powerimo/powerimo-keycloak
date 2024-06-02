package org.powerimo.examples;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class KeycloakMqConsumer {
    private final static String QUEUE_NAME = "keycloak-events";
    private final static String HOST = "localhost";
    private final static int PORT = 5672;

    public static void main(String[] args) throws Exception {
        System.out.println("* KeycloakMqConsumer started");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(
                    String consumerTag,
                    Envelope envelope,
                    AMQP.BasicProperties properties,
                    byte[] body) {

                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Consumed: " + message + "; exchange: " + envelope.getExchange() + "; routingKey: " + envelope.getRoutingKey() + "; envelope: " + envelope);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

}
