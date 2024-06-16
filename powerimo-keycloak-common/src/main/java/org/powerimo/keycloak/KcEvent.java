package org.powerimo.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Event payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KcEvent {
    /**
     * Event type. Possible values: "keycloak.event", "keycloak.admin-event".
     */
    private String eventType;

    /**
     * Keycloak event. E.g. REGISTER, LOGIN
     */
    private String event;

    /**
     * Server ID from the configuration file
     */
    private String serverId;

    /**
     * Keycloak Realm ID (UUID)
     */
    private String realmId;

    /**
     * Keycloak Realm name
     */
    private String realmName;

    /**
     * Keycloak error text
     */
    private String error;

    /**
     * Keycloak user ID
     */
    private String userId;

    /**
     * Event details
     */
    private Map<String, String> details;

    /**
     * User IP address
     */
    private String ipAddress;

    /**
     * Event ID
     */
    private String eventId;

    /**
     * Event representation
     */
    private String representation;

    /**
     * Time when the event was received in the Listener
     */
    @Builder.Default
    private Instant eventTime = Instant.now();

    /**
     * Keycloak event time
     */
    private long time;
}
