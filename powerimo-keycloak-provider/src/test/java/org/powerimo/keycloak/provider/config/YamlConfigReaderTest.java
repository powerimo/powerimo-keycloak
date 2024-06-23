package org.powerimo.keycloak.provider.config;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class YamlConfigReaderTest {

    @Test
    public void readConfigTest() {
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("sample_config.yaml")).getFile();

        YamlConfigReader reader = new YamlConfigReader(filePath);
        var config = reader.readConfig();

        assertNotNull(config);
        assertTrue(config.isEnabled());
        assertEquals("qa", config.getServerId());
        assertNotNull(config.getChannels());
        assertEquals(2, config.getChannels().size());

        var item1 = config.getChannels().get(0);

        assertNotNull(item1);
        assertEquals("channel01", item1.getId());
        assertEquals("prod", item1.getRealmName());
        assertEquals("pwd1", item1.getPassword());
        assertTrue(item1.isEnabled());
        assertEquals("amqp://server01", item1.getUrl());
    }

    @Test
    public void readConfig_NotExistFile() {
        String filePath = "non-existing.yaml";

        YamlConfigReader reader = new YamlConfigReader(filePath);
        var config = reader.readConfig();

        assertNotNull(config);
        assertFalse(config.isEnabled());
    }
}
