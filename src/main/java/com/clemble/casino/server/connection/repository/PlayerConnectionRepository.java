package com.clemble.casino.server.connection.repository;

import com.clemble.casino.server.connection.ServerPlayerConnection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by mavarazy on 8/12/14.
 */
@Repository
public interface PlayerConnectionRepository extends MongoRepository<ServerPlayerConnection, String> {

    List<ServerPlayerConnection> findByConnectionKeyIn(Collection<ConnectionKey> connectionKeys);

    List<ServerPlayerConnection> findByPlayerAndConnectionKeyProviderId(String player, String provider);

    List<ServerPlayerConnection> findByPlayerAndConnectionKey(String player, ConnectionKey key);

    @Query(value = "{'player': ?0, 'connectionKey.providerId': ?1}", count = true)
    Integer countByProvider(String player, String providerId);
}
