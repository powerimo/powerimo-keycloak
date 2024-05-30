package org.powerimo.keycloak.provider.channels;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.config.ChannelConfig;
import org.powerimo.keycloak.provider.config.ChannelConfigImpl;

import static org.junit.jupiter.api.Assertions.*;

class StubChannelTest {
    private StubChannel stubChannel;
    private ChannelConfigImpl channelConfig;

    @BeforeEach
    void setUp() {
        channelConfig = new ChannelConfigImpl();
        channelConfig.setRealmName("QA");

        stubChannel = new StubChannel(channelConfig, new KcListener())
    }

    @Test
    void adminEvent() {
    }

    @Test
    void event() {
    }
}