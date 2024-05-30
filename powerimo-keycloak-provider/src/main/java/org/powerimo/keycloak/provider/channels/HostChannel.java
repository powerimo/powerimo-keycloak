package org.powerimo.keycloak.provider.channels;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.powerimo.keycloak.provider.KcListener;
import org.powerimo.keycloak.provider.PublishingChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HostChannel implements PublishingChannel {
    private static final Logger log = LoggerFactory.getLogger(HostChannel.class);
    private final List<PublishingChannel> channelList = new ArrayList<>();

    @Override
    public void processAdminEvent(AdminEvent adminEvent, KcListener listener) {
        channelList.forEach(channel -> tryPublish(channel, adminEvent, listener));
    }

    @Override
    public void processEvent(Event event, KcListener listener) {
        channelList.forEach(channel -> tryPublish(channel, event, listener));
    }

    private void tryPublish(PublishingChannel channel, Event event, KcListener listener) {
        try {
            channel.processEvent(event, listener);
            log.info("The message has been published on the channel: {}, event={}", channel.getClass().getCanonicalName(), event);
        } catch (Exception ex) {
            log.error("Failed to publish event on the channel: {}; event={}", channel.getClass().getCanonicalName(), event, ex);
        }
    }

    private void tryPublish(PublishingChannel channel, AdminEvent adminEvent, KcListener listener) {
        try {
            channel.processAdminEvent(adminEvent, listener);
            log.info("The message has been published on the channel: {}, event={}", channel.getClass().getCanonicalName(), adminEvent);
        } catch (Exception ex) {
            log.error("Failed to publish event on the channel: {}; event={}", channel.getClass().getCanonicalName(), adminEvent, ex);
        }
    }

    public void addChannel(PublishingChannel channel) {
        channelList.add(channel);
    }

}
