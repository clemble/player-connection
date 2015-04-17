package com.clemble.casino.server.connection.listener;

import com.clemble.casino.server.connection.service.PlayerGraphService;
import com.clemble.casino.server.event.player.SystemPlayerConnectionsFetchedEvent;
import com.clemble.casino.server.event.player.SystemPlayerDiscoveredConnectionEvent;
import com.clemble.casino.server.player.notification.SystemEventListener;
import com.clemble.casino.server.player.notification.SystemNotificationService;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.clemble.casino.utils.Preconditions.checkNotNull;

/**
 * Created by mavarazy on 7/4/14.
 */
public class PlayerGraphPopulatorListener implements SystemEventListener<SystemPlayerConnectionsFetchedEvent> {

    final private Logger LOG = LoggerFactory.getLogger(PlayerGraphPopulatorListener.class);

    final private PlayerGraphService connectionService;
    final private SystemNotificationService notificationService;

    public PlayerGraphPopulatorListener(
        PlayerGraphService connectionService,
        SystemNotificationService notificationService) {
        this.connectionService = checkNotNull(connectionService);
        this.notificationService = checkNotNull(notificationService);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onEvent(SystemPlayerConnectionsFetchedEvent event) {
        boolean added = connectionService.addOwned(event.getPlayer(), event.getConnection());
        LOG.debug("1. Added new owned connection {}", added);
        Set<String> connected =  connectionService.getConnections(event.getPlayer());
        LOG.debug("2. Existing connections {}", connected);
        Set<String> discoveredConnections = Sets.newHashSet(connectionService.getOwners(event.getConnections()));
        LOG.debug("3. All registered users {}", discoveredConnections);
        discoveredConnections.removeAll(connected);
        LOG.debug("4.1. Removing all already connected {}", discoveredConnections);
        for(String discovered : discoveredConnections) {
            LOG.debug("5.1 Connecting with {}", discovered);
            connectionService.connect(event.getPlayer(), discovered);
            LOG.debug("5.2 Sending discover notification {}", discovered);
            notificationService.send(new SystemPlayerDiscoveredConnectionEvent(event.getPlayer(), discovered));
        }
    }

    @Override
    public String getChannel() {
        return SystemPlayerConnectionsFetchedEvent.CHANNEL;
    }

    @Override
    public String getQueueName() {
        return SystemPlayerConnectionsFetchedEvent.CHANNEL + " > player:connection:populator";
    }

}
