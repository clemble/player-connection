package com.clemble.casino.server.connection;

import com.clemble.casino.player.PlayerAware;
import com.clemble.casino.player.PlayerConnection;
import com.clemble.casino.player.PlayerConnectionInvitation;
import org.springframework.data.annotation.Id;

/**
 * @author Anton Oparin (antono@clemble.com)
 */
public class ServerPlayerConnectionInvitation implements PlayerAware {

    @Id
    final private String id;
    final private String to;
    final private String player;
    final private String name;

    public ServerPlayerConnectionInvitation(
        String id,
        String to,
        String player,
        String name
    ) {
        this.player = player;
        this.id = id;
        this.to = to;
        this.name = name;
    }

    public static ServerPlayerConnectionInvitation create(String to, PlayerConnection connection){
        return new ServerPlayerConnectionInvitation(
            to + ":" + connection.getPlayer(),
            to,
            connection.getPlayer(),
            connection.getName()
        );
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

    public String getTo() {
        return to;
    }

    public PlayerConnection toConnection(){
        return new PlayerConnection(player, name);
    }

    public PlayerConnectionInvitation toInvitation() {
        return new PlayerConnectionInvitation(to, toConnection());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerPlayerConnectionInvitation)) return false;

        ServerPlayerConnectionInvitation that = (ServerPlayerConnectionInvitation) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;
        if (player != null ? !player.equals(that.player) : that.player != null) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (player != null ? player.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

}
