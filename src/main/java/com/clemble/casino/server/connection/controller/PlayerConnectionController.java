package com.clemble.casino.server.connection.controller;

import java.util.Set;

import com.clemble.casino.player.PlayerConnection;
import com.clemble.casino.player.PlayerConnectionInvitation;
import com.clemble.casino.player.service.PlayerConnectionService;
import com.clemble.casino.server.ServerController;
import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.clemble.casino.WebMapping;
import static com.clemble.casino.player.PlayerConnectionWebMapping.*;

@RestController
public class PlayerConnectionController implements PlayerConnectionService, ServerController {

    final private ServerPlayerConnectionService connectionService;

    public PlayerConnectionController(ServerPlayerConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public Set<PlayerConnection> myConnections() {
        throw new IllegalAccessError();
    }

    @RequestMapping(value = MY_CONNECTIONS, method = RequestMethod.GET, produces = WebMapping.PRODUCES)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<PlayerConnection> myConnections(@CookieValue("player") String player) {
        return connectionService.getConnections(player);
    }

    @Override
    @RequestMapping(value = PLAYER_CONNECTIONS, method = RequestMethod.GET, produces = WebMapping.PRODUCES)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<PlayerConnection> getConnections(@PathVariable("player") String player) {
        return connectionService.getConnections(player);
    }

}
