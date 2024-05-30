package org.powerimo.keycloak.provider.channels;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.config.ChannelConfig;

public class StubChannel extends AbstractChannel{
    private final static Logger log = Logger.getLogger(StubChannel.class);

    public StubChannel(ChannelConfig config, KcListener listener) {
        setConfig(config);
        setListener(listener);
    }

    @Override
    protected void adminEvent(AdminEvent adminEvent, KcListener listener) {
        log.infof("event: adminEvent=%s, listener=%s", adminEvent, listener);
    }

    @Override
    protected void event(Event event, KcListener listener) {
        log.infof("event: adminEvent=%s, listener=%s", event, listener);
    }

}
