package com.clemble.casino.server.connection.listener;

import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import com.clemble.casino.server.event.player.SystemPlayerCreatedEvent;
import com.clemble.casino.server.event.player.SystemPlayerProfileRegisteredEvent;
import com.clemble.casino.server.event.player.SystemPlayerRegisteredEvent;
import com.clemble.casino.server.player.notification.SystemEventListener;

/**
 * Created by mavarazy on 7/4/14.
 */
public class PlayerConnectionCreationListener implements SystemEventListener<SystemPlayerProfileRegisteredEvent>{

    final private ServerPlayerConnectionService graphService;

    public PlayerConnectionCreationListener(ServerPlayerConnectionService socialNetworkRepository) {
        this.graphService = socialNetworkRepository;
    }

    @Override
    public void onEvent(SystemPlayerProfileRegisteredEvent event) {
        // Step 0. Check Connection was not yet created
        if (graphService.getConnections(event.getPlayer()) != null)
            return;
        // Step 1. Saving connections
        graphService.create(event.getPlayerProfile());
    }

    @Override
    public String getChannel() {
        return SystemPlayerProfileRegisteredEvent.CHANNEL;
    }

    @Override
    public String getQueueName() {
        return SystemPlayerProfileRegisteredEvent.CHANNEL + " > player:connection";
    }

}
