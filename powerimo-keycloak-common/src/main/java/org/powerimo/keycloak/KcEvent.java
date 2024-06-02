package org.powerimo.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KcEvent {
    private String eventType;
    private String event;
    private String server;
    private String realmId;
    private String realmName;
    private String error;
    private String userId;
    private Map<String, String> details;
    private String ipAddress;
    private String eventId;
    private String representation;

    @Builder.Default
    private Instant eventTime = Instant.now();
    private long time;
}
