package com.clemble.casino.server.connection.listener;

import com.clemble.casino.player.event.PlayerConnectionAddEvent;
import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import com.clemble.casino.server.event.player.SystemPlayerConnectionsFetchedEvent;
import com.clemble.casino.server.event.player.SystemPlayerDiscoveredConnectionEvent;
import com.clemble.casino.server.player.notification.ServerNotificationService;
import com.clemble.casino.server.player.notification.SystemEventListener;
import com.clemble.casino.server.player.notification.SystemNotificationService;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static com.clemble.casino.utils.Preconditions.checkNotNull;

/**
 * Created by mavarazy on 7/4/14.
 */
public class PlayerConnectionPopulatorListener implements SystemEventListener<SystemPlayerConnectionsFetchedEvent> {

    final private Logger LOG = LoggerFactory.getLogger(PlayerConnectionPopulatorListener.class);

    final private ServerPlayerConnectionService connectionService;
    final private ServerNotificationService notificationService;
    final private SystemNotificationService systemNotificationService;

    public PlayerConnectionPopulatorListener(
            ServerPlayerConnectionService connectionService,
            ServerNotificationService notificationService,
            SystemNotificationService systemNotificationService) {
        this.connectionService = checkNotNull(connectionService);
        this.notificationService = notificationService;
        this.systemNotificationService = checkNotNull(systemNotificationService);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onEvent(SystemPlayerConnectionsFetchedEvent event) {
        boolean added = connectionService.addOwned(event.getPlayer(), event.getConnection());
        LOG.debug("1. Added new owned connection {}", added);

        Set<String> connected = connectionService.
            getConnections(event.getPlayer()).
            stream().
            map((connection) -> connection.getPlayer()).
            collect(Collectors.toSet());
        LOG.debug("2. Existing connections {}", connected);

        Set<String> discoveredConnections = Sets.newHashSet(connectionService.getOwners(event.getConnections()));
        LOG.debug("3. All registered users {}", discoveredConnections);

        discoveredConnections.removeIf((discovered) -> connected.contains(discovered));
        LOG.debug("4.1. Removing all already connected {}", discoveredConnections);

        if (!discoveredConnections.isEmpty()) {
            for (String discovered : discoveredConnections) {
                LOG.debug("5.1 Connecting with {}", discovered);
                connectionService.
                    connect(event.getPlayer(), discovered).
                    forEach((connection) -> notificationService.send(new PlayerConnectionAddEvent(connection.getPlayer(), connection.toConnection())));
                LOG.debug("5.2 Sending discover notification {}", discovered);
                systemNotificationService.send(new SystemPlayerDiscoveredConnectionEvent(event.getPlayer(), discovered));
            }
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
