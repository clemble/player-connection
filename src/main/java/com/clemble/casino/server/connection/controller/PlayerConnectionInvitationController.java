package com.clemble.casino.server.connection.controller;

import com.clemble.casino.error.ClembleErrorCode;
import com.clemble.casino.error.ClembleException;
import com.clemble.casino.player.PlayerConnection;
import com.clemble.casino.player.PlayerConnectionInvitation;
import com.clemble.casino.player.event.PlayerConnectionAddEvent;
import com.clemble.casino.player.event.PlayerInvitedConnectionEvent;
import com.clemble.casino.player.service.PlayerConnectionInvitationService;
import com.clemble.casino.server.ServerController;

import com.clemble.casino.server.connection.ServerPlayerConnectionInvitation;
import com.clemble.casino.server.connection.repository.PlayerConnectionInvitationRepository;
import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import com.clemble.casino.server.player.notification.ServerNotificationService;

import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static com.clemble.casino.player.PlayerConnectionWebMapping.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mavarazy on 11/11/14.
 */
@RestController
public class PlayerConnectionInvitationController implements PlayerConnectionInvitationService, ServerController {

    final private ServerNotificationService notificationService;
    final private ServerPlayerConnectionService connectionService;
    final private PlayerConnectionInvitationRepository invitationRepository;

    public PlayerConnectionInvitationController(
        ServerNotificationService notificationService,
        ServerPlayerConnectionService connectionService,
        PlayerConnectionInvitationRepository invitationRepository){
        this.invitationRepository = invitationRepository;
        this.notificationService = notificationService;
        this.connectionService = connectionService;
    }


    @Override
    public List<PlayerConnectionInvitation> myInvitations() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = GET, value = MY_INVITATIONS)
    @ResponseStatus(OK)
    public List<PlayerConnectionInvitation> myInvitations(@CookieValue("player") String me) {
        return invitationRepository.
            findByTo(me).
            stream().
            map((serverInvitation) -> serverInvitation.toInvitation()).
            collect(Collectors.toList());
    }

    @Override
    public PlayerConnectionInvitation invite(String player) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = POST, value = MY_INVITATIONS)
    @ResponseStatus(CREATED)
    public PlayerConnectionInvitation invite(@CookieValue("player") String me, @RequestBody String invitee) {
        List<ServerPlayerConnectionInvitation> invitations = invitationRepository.findByToAndPlayer(me, invitee);
        if (invitations.isEmpty()) {

            String myName = connectionService.getName(me);
            PlayerConnection myConnection = new PlayerConnection(me, myName);
            ServerPlayerConnectionInvitation invitation = ServerPlayerConnectionInvitation.create(invitee, myConnection);
            // Case 1. If there is no pending invitation from receiver, add new invitation
            invitationRepository.save(invitation);
            // Send notification
            notificationService.send(new PlayerInvitedConnectionEvent(invitee, myConnection));
            // Sending player invitation
            return invitation.toInvitation();
        } else {
            // Case 2. If there is a pending invitation, just reply positive
            return reply(me, invitee, true);
        }
    }

    @Override
    public PlayerConnectionInvitation reply(String player, boolean accept) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = POST, value = MY_INVITATIONS_REPLY)
    @ResponseStatus(CREATED)
    public PlayerConnectionInvitation reply(@CookieValue("player") String me, @PathVariable("player") String invitee, @RequestBody boolean accept) {
        // Step 1. Checking invitation exists
        // Key is actually player - me, since player is the sender
        List<ServerPlayerConnectionInvitation> invitation = invitationRepository.findByToAndPlayer(me, invitee);
        if (invitation.isEmpty())
            throw ClembleException.withServerError(ClembleErrorCode.PlayerNoInvitation);
        ServerPlayerConnectionInvitation myInvitation = invitation.get(0);
        // Step 2. Removing invitation
        invitation.remove(myInvitation);
        // Step 3. If it's an accept add connection
        if(accept) {
            // Step 3.1. Saving and sending add notification to the player
            connectionService.
                connect(me, invitee).
                forEach((serverConnection) -> {
                    if (serverConnection.getPlayer() == me)
                        notificationService.send(new PlayerConnectionAddEvent(invitee, serverConnection.toConnection()));
                });
        }
        // Step 3.3 Sending notification to player
        return myInvitation.toInvitation();
    }
}
