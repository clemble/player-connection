package com.clemble.casino.server.connection.service;

import com.clemble.casino.player.PlayerConnection;
import com.clemble.casino.player.PlayerProfile;
import com.clemble.casino.player.service.PlayerConnectionService;
import com.clemble.casino.server.connection.ServerPlayerConnection;
import com.clemble.casino.server.connection.repository.PlayerConnectionRepository;
import com.google.common.collect.ImmutableList;
import org.springframework.social.connect.ConnectionKey;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mavarazy on 8/12/14.
 */
public class ServerPlayerConnectionService implements PlayerConnectionService {

    final private String PROVIDER = "clemble";

    final private PlayerConnectionRepository graphRepository;

    public ServerPlayerConnectionService(PlayerConnectionRepository graphRepository){
        this.graphRepository = graphRepository;
    }

    @Override
    public Set<PlayerConnection> myConnections() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<PlayerConnection> getConnections(String me) {
        // Step 1. Fetch player graph
        return graphRepository.findByPlayerAndConnectionKeyProviderId(me, PROVIDER).
            stream().
            // Step 2. Return related connections
            map((connection) -> connection.toConnection()).
            collect(Collectors.toSet());
    }

    public boolean addOwned(String player, ConnectionKey connectionKey) {
        // Step 1. Adding record for owned connection
        ServerPlayerConnection playerConnection = new ServerPlayerConnection(null, player, connectionKey, null);
        // Step 2. Adding owned to connectionKeys
        ServerPlayerConnection savedConnection = graphRepository.save(playerConnection);
        return savedConnection != null;
    }

    public boolean create(PlayerProfile profile) {
        ServerPlayerConnection newConnection = new ServerPlayerConnection(null, profile.getPlayer(), new ConnectionKey(PROVIDER, profile.getPlayer()), profile.getFirstName());
        return graphRepository.save(newConnection) != null;
    }

    public Iterable<String> getOwners(Collection<ConnectionKey> connections) {
        return graphRepository.
            findByConnectionKeyIn(connections).
            stream().
            map((connection) -> connection.getPlayer()).
            collect(Collectors.toList());
    }

    public Collection<ServerPlayerConnection> connect(String a, String b) {
        // Step 1. Checking they already connected
        List<ServerPlayerConnection> connections = graphRepository.findByPlayerAndConnectionKey(a, new ConnectionKey(PROVIDER, b));
        if (!connections.isEmpty())
            return Collections.emptyList();
        // Step 2. Generating new connections
        String aName = getName(a);
        String bName = getName(b);
        // Step 2.
        ServerPlayerConnection aToB = new ServerPlayerConnection(null, a, new ConnectionKey(PROVIDER, b), bName);
        ServerPlayerConnection bToA = new ServerPlayerConnection(null, b, new ConnectionKey(PROVIDER, a), aName);
        return graphRepository.save(ImmutableList.of(aToB, bToA));
    }

    public String getName(String player) {
        List<ServerPlayerConnection> aConnection = graphRepository.findByPlayerAndConnectionKey(player, new ConnectionKey(PROVIDER, player));
        return aConnection.size() == 1 ? aConnection.get(0).getName() : null;
    }
}
