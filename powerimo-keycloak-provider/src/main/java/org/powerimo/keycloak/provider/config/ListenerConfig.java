package org.powerimo.keycloak.provider.config;

import java.util.List;

public interface ListenerConfig {
    boolean isEnabled();
    String getServerId();
    List<ChannelConfig> getChannelConfigs();
}
