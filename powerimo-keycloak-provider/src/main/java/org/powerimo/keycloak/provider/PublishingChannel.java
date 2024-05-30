package org.powerimo.keycloak.provider;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

public interface PublishingChannel {
    void processEvent(Event event, KcListener listener);
    void processAdminEvent(AdminEvent adminEvent, KcListener listener);
}
