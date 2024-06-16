package org.powerimo.keycloak;

/**
 * Constants for the Powerimo Keycloak libraries and applications
 */
public class KcConst {
    /**
     * EventType: regular Keycloak event, such as "REGISTER", "LOGIN"
     */
    public static final String KC_EVENT = "keycloak.event";

    /**
     * EventType: Admin Event,
     */
    public static final String KC_ADMIN_EVENT = "keycloak.admin-event";

    public static final String ALL_REALMS = "ALL_REALMS";

    public static final String EVENT_TYPE_HEADER = "Event-Type";
    public static final String SERVER_ID_HEADER = "Server-Id";
}
