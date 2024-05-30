package org.powerimo.keycloak.provider.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class YamlConfig implements ListenerConfig {
    private boolean enabled;
    private String serverId;
    private List<ChannelConfigImpl> channels = new ArrayList<>();
    private String configFilePath;

    @Override
    public List<ChannelConfig> getChannelConfigs() {
        return new ArrayList<>(channels);
    }
}
