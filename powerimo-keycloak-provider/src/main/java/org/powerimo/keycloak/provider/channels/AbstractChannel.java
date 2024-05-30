package org.powerimo.keycloak.provider.channels;

import lombok.Getter;
import lombok.Setter;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.powerimo.keycloak.KcConst;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.PublishingChannel;
import org.powerimo.keycloak.provider.config.ChannelConfig;

@Getter
@Setter
public abstract class AbstractChannel implements PublishingChannel {
    private final static Logger log = Logger.getLogger(AbstractChannel.class);
    private ChannelConfig config;
    private KcListener listener;

    protected boolean isEventAcceptable(Object object) {
        AdminEvent adminEventObject;
        Event eventObject;
        String realmId;

        if (object instanceof AdminEvent) {
            adminEventObject = (AdminEvent) object;
            realmId = adminEventObject.getRealmId();
        } else if (object instanceof Event) {
            eventObject = (Event) object;
            realmId = eventObject.getRealmId();
        } else {
            return false;
        }

        if (config == null)
            return false;

        // if the channel does not have realmName filter or this is empty then the event is accepted
        if (config.getRealmName() == null || config.getRealmName().isEmpty() || config.getRealmName().equals(KcConst.ALL_REALMS)) {
            log.trace("Channel " + getClass().getSimpleName() + " realm name is null or empty. All events will be published.");
            return true;
        }

        if (listener == null) {
            log.error("Listener is null. Channel " + getClass().getSimpleName() + " realm name is " + config.getRealmName());
            return false;
        }

        // extract Realm Name from the event;
        var realmName = listener.getSession().realms().getRealm(realmId);

        if (config.getRealmName().equals(realmName)) {
            return true;
        }

        return false;
    }

    @Override
    public void processAdminEvent(AdminEvent adminEvent, KcListener listener) {
        var isEventAcceptable = isEventAcceptable(adminEvent);
        if (isEventAcceptable) {
            adminEvent(adminEvent, listener);
        }
    }

    @Override
    public void processEvent(Event event, KcListener listener) {
        var isEventAcceptable = isEventAcceptable(event);
        if (isEventAcceptable) {
            event(event, listener);
        }
    }

    protected abstract void adminEvent(AdminEvent adminEvent, KcListener listener);
    protected abstract void event(Event event, KcListener listener);
}
