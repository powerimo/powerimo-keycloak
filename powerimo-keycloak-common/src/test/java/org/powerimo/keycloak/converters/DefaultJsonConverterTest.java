package org.powerimo.keycloak.converters;

import org.powerimo.keycloak.KcConst;
import org.powerimo.keycloak.KcEvent;
import org.powerimo.keycloak.KcUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultJsonConverterTest {

    @org.junit.jupiter.api.Test
    void deserialize() {
        var converter = new DefaultJsonSerializer();
        var sample = KcUtils.loadResource("sample_event.json");

        assertNotNull(sample);
        KcEvent event = converter.deserializeEvent(sample);

        assertNotNull(event);
        assertEquals(KcConst.KC_EVENT, event.getEventType());
    }

    @org.junit.jupiter.api.Test
    void serialize() {
        var converter = new DefaultJsonSerializer();
        var event = KcEvent.builder()
                .server("localhost")
                .eventType(KcConst.KC_EVENT)
                .realmId("TEST")
                .event("SAMPLE_EVENT")
                .eventTime(LocalDateTime.of(2024, 5, 29, 9, 18, 36, 1).toInstant(ZoneOffset.UTC))
                .build();
        var byteData = converter.serialize(event);
        String s = new String(byteData, StandardCharsets.UTF_8);

        assertNotNull(byteData);

        var sample = KcUtils.loadResource("sample_event.json");
        assertEquals(sample, s);
    }
}