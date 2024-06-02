package org.powerimo.keycloak.provider.config;

public interface ChannelConfig {
    boolean isEnabled();
    String getId();
    String getChannelClassName();
    String getUrl();
    String getUser();
    String getPassword();
    String getRealmName();
    String getExchange();
    String getRoutingKey();
}
