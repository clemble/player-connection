package com.clemble.casino.server.connection.repository;

import com.clemble.casino.server.connection.ServerPlayerConnection;
import com.clemble.casino.server.connection.ServerPlayerConnectionInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by mavarazy on 11/11/14.
 */
public interface PlayerConnectionInvitationRepository extends MongoRepository<ServerPlayerConnectionInvitation, String> {

    List<ServerPlayerConnectionInvitation> findByTo(String player);

    List<ServerPlayerConnectionInvitation> findByToAndPlayer(String receiver, String sender);

}
