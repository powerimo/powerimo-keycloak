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
    public static final String SPI_ID = "mq-sender";
    public static final String CONFIG_FILE_PROPERTY = SPI_ID + "-config-file";
    private YamlConfigReader yamlConfigReader;

    @Override
    public KcListener create(KeycloakSession keycloakSession) {
         var config = yamlConfigReader.readConfig();

        // create a listener
        var listener = new KcListener(keycloakSession, config);

        // create default Publishing channel as a parent to child channels
        HostChannel hostChannel = new HostChannel();
        listener.setPublishingChannel(hostChannel);

        // setup child channels
        for (var channelConfig : config.getChannels()) {
            var childChannel = createPublishingChannel(channelConfig, listener);
            hostChannel.addChannel(childChannel);
            log.debugf("Created publishing channel: %s; config: %s", childChannel, channelConfig);
        }

        log.debugf("Listener created");
        return listener;
    }

    @Override
    public void init(Config.Scope scope) {
        log.infof("%s initialized...", SPI_ID);
        var names = scope.getPropertyNames();
        log.info(names.toString());

        var configFilePath = scope.get(CONFIG_FILE_PROPERTY);
        if (configFilePath == null) {
            configFilePath = YamlConfigReader.DEFAULT_PATH;
        }
        yamlConfigReader  = new YamlConfigReader(configFilePath);

        var isConfigFileExist = yamlConfigReader.isConfigFileExist();
        log.infof("Config file is exist: %s; path: %s", isConfigFileExist, configFilePath);

        log.infof("%s initialization complete.", SPI_ID);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        log.infof("%s factory started", SPI_ID);
    }

    @Override
    public void close() {
        log.infof("%s factory closed", SPI_ID);
    }

    @Override
    public String getId() {
        return SPI_ID;
    }

    private PublishingChannel createPublishingChannel(@NonNull ChannelConfig config, @NonNull KcListener listener) {
        PublishingChannel publishingChannel;

        try {
            if (Objects.equals(config.getChannelClassName(), RabbitMqChannel.class.getName())) {
                publishingChannel = new RabbitMqChannel(config, listener);
            } else if (Objects.equals(config.getChannelClassName(), StubChannel.class.getName())) {
                publishingChannel = new StubChannel(config, listener);
            } else {
                publishingChannel = new RabbitMqChannel(config, listener);
            }
        } catch (Exception ex) {
            throw new PowerimoKeycloakProviderException("Exception on creating channel", ex);
        }

        return publishingChannel;
    }
}
