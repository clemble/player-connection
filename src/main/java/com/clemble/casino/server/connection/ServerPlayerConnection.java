package com.clemble.casino.server.connection;

import com.clemble.casino.player.PlayerAware;
import com.clemble.casino.player.PlayerConnection;
import org.springframework.data.annotation.Id;
import org.springframework.social.connect.ConnectionKey;

/**
 * Created by mavarazy on 11/11/14.
 */
public class ServerPlayerConnection implements PlayerAware {

    @Id
    final private String id;
    final private String player;
    final private ConnectionKey connectionKey;
    final private String name;

    public ServerPlayerConnection(
        String id,
        String player,
        ConnectionKey connectionKey,
        String name
    ) {
        this.id = id;
        this.name = name;
        this.player = player;
        this.connectionKey = connectionKey;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public ConnectionKey getConnectionKey() {
        return connectionKey;
    }

    public PlayerConnection toConnection() {
        return new PlayerConnection(connectionKey.getProviderUserId(), name);
    }

}
