package org.powerimo.keycloak.provider.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChannelConfigImpl implements ChannelConfig{
    private String realmName;
    private String channelClassName;
    private boolean enabled;
    private String id;
    private String url;
    private String user;
    private String password;

    @Override
    public String getChannelClassName() {
        return channelClassName;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
