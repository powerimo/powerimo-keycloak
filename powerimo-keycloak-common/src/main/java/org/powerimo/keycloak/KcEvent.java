package org.powerimo.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KcEvent {
    private String eventType;
    private String event;
    private String server;
    private String objectType;
    private UUID objectId;
    private String realmId;
    private String realmName;
    private String error;

    @Builder.Default
    private Instant eventTime = Instant.now();
}
