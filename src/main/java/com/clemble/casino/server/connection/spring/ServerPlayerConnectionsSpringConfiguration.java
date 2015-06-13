package com.clemble.casino.server.connection.spring;

import com.clemble.casino.player.PlayerConnectionInvitation;
import com.clemble.casino.server.connection.repository.PlayerConnectionRepository;
import com.clemble.casino.server.connection.repository.PlayerConnectionInvitationRepository;
import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import com.clemble.casino.server.spring.common.MongoSpringConfiguration;
import com.clemble.casino.server.spring.common.SpringConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * Created by mavarazy on 8/12/14.
 */
public class ServerPlayerConnectionsSpringConfiguration {

    @Configuration
    @Import(MongoSpringConfiguration.class)
    public static class MongoPlayerConnectionsSpringConfiguration implements SpringConfiguration {

        @Autowired
        public MongoTemplate mongoTemplate;

        @Bean
        public PlayerConnectionRepository mongoPlayerConnectionsRepository(MongoRepositoryFactory mongoRepositoryFactory) {
            return mongoRepositoryFactory.getRepository(PlayerConnectionRepository.class);
        }

        @Bean
        public PlayerConnectionInvitationRepository playerFriendInvitationRepository(MongoRepositoryFactory mongoRepositoryFactory) {
            return mongoRepositoryFactory.getRepository(PlayerConnectionInvitationRepository.class);
        }

        @Bean
        public ServerPlayerConnectionService playerGraphService(PlayerConnectionRepository playerConnectionsRepository) {
            return new ServerPlayerConnectionService(playerConnectionsRepository);
        }

        @PostConstruct
        public void insureIndex() {
            // Step 1. Creating invitation index
            Index invitationIndex = new Index().
                on("player", Sort.Direction.DESC).
                on("to", Sort.Direction.DESC).
                unique(Index.Duplicates.DROP);
            // Step 2. Saving connection invitation
            mongoTemplate.
                indexOps(PlayerConnectionInvitation.class).
                ensureIndex(invitationIndex);
        }

    }

}
