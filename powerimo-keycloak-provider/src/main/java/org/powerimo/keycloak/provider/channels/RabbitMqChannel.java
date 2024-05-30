package org.powerimo.keycloak.provider.channels;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.config.ChannelConfig;

public class RabbitMqChannel extends AbstractChannel {

    public RabbitMqChannel(ChannelConfig channelConfig, KcListener listener) {
        this.setConfig(channelConfig);
        this.setListener(listener);
    }


    @Override
    protected void adminEvent(AdminEvent adminEvent, KcListener listener) {

    }

    @Override
    protected void event(Event event, KcListener listener) {

    }
}
