package org.powerimo.keycloak.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.powerimo.keycloak.provider.channels.HostChannel;
import org.powerimo.keycloak.provider.config.ListenerConfig;

import java.util.UUID;

@JBossLog
@Getter
@Setter
public class KcListener implements EventListenerProvider {
    private final KeycloakSession session;
    private String realmName;
    private PublishingChannel publishingChannel;
    private ListenerConfig listenerConfig;

    public KcListener(KeycloakSession session, ListenerConfig listenerConfig) {
        this.session = session;
        publishingChannel = new HostChannel();
        this.listenerConfig = listenerConfig;
    }

    @Override
    public void onEvent(Event event) {
        log.infof("event %s", event);
        if (publishingChannel == null) {
            log.warn("Publisher is not set. The message will not be send.");
            return;
        }
        publishingChannel.processEvent(event, this);
    }

    @Override
    public void onEvent(AdminEvent event, boolean b) {
        log.infof("admin event %s", event);

        if (publishingChannel == null) {
            log.warn("Publisher is not set. The message will not be send.");
            return;
        }
        publishingChannel.processAdminEvent(event, this);
    }

    @Override
    public void close() {

    }

    public String extractRealmName(Event event) {
        if (realmName == null) {
            String realmId = event.getRealmId();
            RealmModel realm = session.realms().getRealm(realmId);
            realmName = realm.getName();
        }
        return realmName;
    }

    public String extractRealmName(AdminEvent event) {
        if (realmName == null) {
            String realmId = event.getRealmId();
            RealmModel realm = session.realms().getRealm(realmId);
            realmName = realm.getName();
        }
        return realmName;
    }

    public String extractRealmName(String realmId) {
        RealmModel realm = session.realms().getRealm(realmId);
        return realm.getName();
    }

    public static UUID extractUUID(String s) {
        if (s == null)
            return null;
        var pos = s.indexOf("/");
        String strUUID = s.substring(pos + 1);
        try {
            return UUID.fromString(strUUID);
        } catch (Exception ex) {
            log.errorf("Couldn't extract UUID from string: %s", s);
        }
        return null;
    }
}
