package org.powerimo.keycloak.provider.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.powerimo.keycloak.KcConst;
import org.powerimo.keycloak.MessageSerializer;
import org.powerimo.keycloak.converters.DefaultJsonSerializer;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.KcListenerFactory;
import org.powerimo.keycloak.provider.PowerimoKeycloakProviderException;
import org.powerimo.keycloak.provider.config.ChannelConfig;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RabbitMqChannel extends AbstractChannel {
    private final static Logger log = Logger.getLogger(RabbitMqChannel.class);
    private final ConnectionFactory connectionFactory;
    private final MessageSerializer serializer;

    public RabbitMqChannel(ChannelConfig channelConfig, KcListener listener) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        this.setConfig(channelConfig);
        this.setListener(listener);

        serializer = new DefaultJsonSerializer();

        connectionFactory = new ConnectionFactory();
        connectionFactory.setUri(channelConfig.getUrl());
    }


    @Override
    protected void adminEvent(AdminEvent adminEvent, KcListener listener) {
        try {
            var data = convert(adminEvent);
            var routingKey = KcConst.KC_ADMIN_EVENT;
            send(data, getConfig().getExchange(), routingKey, adminEvent.getClass().getSimpleName());
        } catch (Exception ex) {
            throw new PowerimoKeycloakProviderException("Exception during event processing", ex);
        }
    }

    @Override
    protected void event(Event event, KcListener listener) {
        try {
            var data = convert(event);
            var routingKey = KcConst.KC_EVENT;
            send(data, getConfig().getExchange(), routingKey, event.getClass().getSimpleName());
        } catch (Exception ex) {
            throw new PowerimoKeycloakProviderException("Exception during event processing", ex);
        }
    }

    private void send(Object payload, String exchangeName, String routingKey, String eventType) throws JsonProcessingException {
        var data = serializer.serialize(payload);

        var propertiesBuilder = new AMQP.BasicProperties.Builder();

        Map<String, Object> headers = new HashMap<>();
        headers.put(KcConst.EVENT_TYPE_HEADER, eventType);
        headers.put(KcConst.SERVER_ID_HEADER, getListener().getListenerConfig().getServerId());

        var properties = propertiesBuilder
                .appId(KcListenerFactory.SPI_ID)
                .headers(headers)
                .contentEncoding(StandardCharsets.UTF_8.name())
                .contentType("application/json")
                .build();

        send(data, exchangeName, routingKey, properties);
    }

    private void send(byte[] payload , String exchangeName, String routingKey, AMQP.BasicProperties properties) {
        try (Connection connection = connectionFactory.newConnection()) {
            Channel channel = connection.createChannel();
            channel.basicPublish(exchangeName, routingKey, properties, payload);
            String s = new String(payload, StandardCharsets.UTF_8);
            log.infof("MQ message sent: %s; properties: %s", s, properties);
        } catch (Exception ex) {
            throw new PowerimoKeycloakProviderException("Exception on send message", ex);
        }
    }
}
