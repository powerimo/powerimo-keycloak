package org.powerimo.keycloak.provider;

import lombok.NonNull;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.powerimo.keycloak.provider.channels.HostChannel;
import org.powerimo.keycloak.provider.channels.RabbitMqChannel;
import org.powerimo.keycloak.provider.channels.StubChannel;
import org.powerimo.keycloak.provider.config.ChannelConfig;
import org.powerimo.keycloak.provider.config.YamlConfigReader;

import java.util.Objects;

public class KcListenerFactory implements EventListenerProviderFactory {
    private static final Logger log = Logger.getLogger(KcListenerFactory.class);
    private YamlConfigReader yamlConfigReader;

    @Override
    public KcListener create(KeycloakSession keycloakSession) {
        // create a listener
        var listener = new KcListener(keycloakSession, yamlConfigReader.readConfig());

        // create default Publishing channel as a parent to child channels
        HostChannel hostChannel = new HostChannel();
        listener.setPublishingChannel(hostChannel);

        // setup child channels
        var config = yamlConfigReader.readConfig();
        for (var channelConfig : config.getChannels()) {
            var childChannel = createPublishingChannel(channelConfig, listener);
            hostChannel.addChannel(childChannel);
            log.infof("Created publishing channel: %s; config: %s", childChannel, channelConfig);
        }

        log.info("Listener created");
        return listener;
    }

    @Override
    public void init(Config.Scope scope) {
        log.info("Powerimo Event Listener initialized...");
        var names = scope.getPropertyNames();
        log.info(names.toString());

        var configFilePath = scope.get("powerimo-event-listener.config-path");
        if (configFilePath != null) {
            configFilePath = YamlConfigReader.DEFAULT_PATH;
        }
        yamlConfigReader  = new YamlConfigReader(configFilePath);

        log.info("Powerimo Event Listener initialization complete.");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        log.info("powerimo-event-listener factory started");
    }

    @Override
    public void close() {
        log.info("powerimo-event-listener factory closed");
    }

    @Override
    public String getId() {
        return "powerimo-event-listener";
    }

    private PublishingChannel createPublishingChannel(@NonNull ChannelConfig config, @NonNull KcListener listener) {
        PublishingChannel publishingChannel;

        if (Objects.equals(config.getChannelClassName(), RabbitMqChannel.class.getName())) {
            publishingChannel = new RabbitMqChannel(config, listener);
        } else if (Objects.equals(config.getChannelClassName(),StubChannel.class.getName())) {
            publishingChannel = new StubChannel(config, listener);
        } else {
            publishingChannel = new RabbitMqChannel(config, listener);
        }

        return publishingChannel;
    }
}
