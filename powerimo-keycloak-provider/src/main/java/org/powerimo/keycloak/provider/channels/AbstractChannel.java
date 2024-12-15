package org.powerimo.keycloak.provider.channels;

import lombok.Getter;
import lombok.Setter;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.powerimo.keycloak.KcConst;
import org.powerimo.keycloak.KcEvent;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.PublishingChannel;
import org.powerimo.keycloak.provider.config.ChannelConfig;

import java.util.HashMap;
import java.util.Map;

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
        var realm = listener.getSession().realms().getRealm(realmId);
        if (realm == null) {
            log.debugf("Realm does not exist. Channel " + getClass().getSimpleName() + " realm name is " + config.getRealmName());
            return false;
        }
        log.debugf("resolved realm name: %s", realm.getName());

        if (realm.getName().equalsIgnoreCase(config.getRealmName())) {
            return true;
        }

        return false;
    }

    @Override
    public void processAdminEvent(AdminEvent adminEvent, KcListener listener) {
        var isEventAcceptable = isEventAcceptable(adminEvent);
        if (isEventAcceptable) {
            log.debugf("message is acceptable: %s for config %s", eventToString(adminEvent), config);
            adminEvent(adminEvent, listener);
        } else {
            log.debugf("message is not acceptable: %s for config %s", eventToString(adminEvent), config);
        }

    }

    @Override
    public void processEvent(Event event, KcListener listener) {
        var isEventAcceptable = isEventAcceptable(event);
        if (isEventAcceptable) {
            log.debugf("message is acceptable: %s for config %s", eventToString(event), config);
            event(event, listener);
        } else {
            log.debugf("message is not acceptable: %s for config %s", eventToString(event), config);
        }
    }

    protected abstract void adminEvent(AdminEvent adminEvent, KcListener listener);
    protected abstract void event(Event event, KcListener listener);

    public KcEvent convert(AdminEvent event) {
        Map<String, String> details = new HashMap<>();
        details.put("resource_path", event.getResourcePath());

        var realmId = event.getRealmId();
        String userId = null;
        String ip = null;

        // fill Auth details into event details
        if (event.getAuthDetails() != null) {
            if (realmId == null) {
                realmId = event.getAuthDetails().getRealmId();
            }
            userId = event.getAuthDetails().getUserId();
            ip = event.getAuthDetails().getIpAddress();

            details.put("client_id", event.getAuthDetails().getClientId());
            details.put("ip_address", ip);
            details.put("realmId", realmId);
            details.put("user_id", userId);
        }

        var realmName = realmId == null ? null : getListener().extractRealmName(realmId);

        return KcEvent.builder()
                .eventType(KcConst.KC_ADMIN_EVENT)
                .serverId(getListener().getListenerConfig().getServerId())
                .realmId(event.getRealmId())
                .realmName(realmName)
                .error(event.getError())
                .event(event.getOperationType().name())
                .time(event.getTime())
                .eventId(event.getId())
                .representation(event.getRepresentation())
                .details(details)
                .ipAddress(ip)
                .userId(userId)
                .build();
    }

    public KcEvent convert(Event event) {
        return KcEvent.builder()
                .eventType(KcConst.KC_EVENT)
                .realmId(event.getRealmId())
                .realmName(getListener().extractRealmName(event))
                .error(event.getError())
                .event(event.getType().name())
                .userId(event.getUserId())
                .serverId(getListener().getListenerConfig().getServerId())
                .time(event.getTime())
                .details(event.getDetails())
                .ipAddress(event.getIpAddress())
                .eventId(event.getId())
                .build();
    }

    public static String eventToString(Event event) {
        return "Event(" +
                "realmId=" + event.getRealmId() +
                ",error=" + event.getError() +
                ",type=" + event.getType() +
                ",userId=" + event.getUserId() +
                ")";
    }

    public static String eventToString(AdminEvent event) {
        return "Event(" +
                "realmId=" + event.getRealmId() +
                ",error=" + event.getError() +
                ",operationType=" + event.getOperationType() +
                ")";
    }
}
