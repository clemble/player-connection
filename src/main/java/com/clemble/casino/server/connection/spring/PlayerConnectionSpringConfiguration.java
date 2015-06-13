package com.clemble.casino.server.connection.spring;

import com.clemble.casino.player.service.PlayerConnectionService;
import com.clemble.casino.server.connection.controller.PlayerConnectionInvitationController;
import com.clemble.casino.server.connection.listener.PlayerConnectionCreationListener;
import com.clemble.casino.server.connection.listener.PlayerConnectionPopulatorListener;
import com.clemble.casino.server.connection.repository.PlayerConnectionInvitationRepository;
import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import com.clemble.casino.server.player.notification.ServerNotificationService;
import com.clemble.casino.server.player.notification.SystemNotificationService;
import com.clemble.casino.server.spring.common.CommonSpringConfiguration;
import com.clemble.casino.server.connection.controller.PlayerConnectionController;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.util.HashMap;

import static com.clemble.casino.server.spring.common.ConnectionClientSpringConfiguration.Default.*;

@Configuration
@Import({ CommonSpringConfiguration.class, ServerPlayerConnectionsSpringConfiguration.MongoPlayerConnectionsSpringConfiguration.class })
public class PlayerConnectionSpringConfiguration {

    @Bean
    public PlayerConnectionCreationListener playerConnectionNetworkCreationListener(
            ServerPlayerConnectionService playerRepository
    ) {
        PlayerConnectionCreationListener networkCreationService = new PlayerConnectionCreationListener(playerRepository);
        return networkCreationService;
    }

    @Bean
    public PlayerConnectionPopulatorListener socialNetworkConnectionCreatorListener(
            ServerPlayerConnectionService playerRepository,
            ServerNotificationService notificationService,
            SystemNotificationService systemNotificationService) {
        PlayerConnectionPopulatorListener connectionCreatorListener = new PlayerConnectionPopulatorListener(playerRepository, notificationService, systemNotificationService);
        return connectionCreatorListener;
    }

    @Bean
    public PlayerConnectionController playerConnectionController(ServerPlayerConnectionService connectionService) {
        return new PlayerConnectionController(connectionService);
    }

    @Bean
    public PlayerConnectionInvitationController playerFriendInvitationServiceController(
        ServerNotificationService notificationService,
        ServerPlayerConnectionService connectionService,
        PlayerConnectionInvitationRepository invitationRepository) {
        return new PlayerConnectionInvitationController(
            notificationService,
            connectionService,
            invitationRepository);
    }

    @Bean
    public SimpleMessageListenerContainer connectionServiceListener(
        PlayerConnectionController playerConnectionController,
        @Value("${clemble.service.notification.system.user}") String user,
        @Value("${clemble.service.notification.system.password}") String password,
        @Value("${SYSTEM_NOTIFICATION_SERVICE_HOST}") String host) throws Exception {

        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        connectionFactory.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("CL account client %d").build());

        CachingConnectionFactory springConnectionFactory = new CachingConnectionFactory(connectionFactory);

        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setExchange(CONNECTION_EXCHANGE);
        rabbitTemplate.setRoutingKey(CONNECTION_ROUTING_KEY);
        rabbitTemplate.setConnectionFactory(springConnectionFactory);

        RabbitAdmin rabbitAdmin = new RabbitAdmin(springConnectionFactory);
        rabbitAdmin.declareExchange(new DirectExchange(CONNECTION_EXCHANGE, true, false));
        rabbitAdmin.declareQueue(new Queue(CONNECTION_QUEUE, true));
        rabbitAdmin.declareBinding(new Binding(CONNECTION_QUEUE, Binding.DestinationType.QUEUE, CONNECTION_EXCHANGE, CONNECTION_ROUTING_KEY, new HashMap<String, Object>()));

        AmqpInvokerServiceExporter serviceExporter = new AmqpInvokerServiceExporter();
        serviceExporter.setAmqpTemplate(rabbitTemplate);
        serviceExporter.setServiceInterface(PlayerConnectionService.class);
        serviceExporter.setService(playerConnectionController);

        SimpleMessageListenerContainer accountServiceListener = new SimpleMessageListenerContainer();
        accountServiceListener.setMessageListener(serviceExporter);
        accountServiceListener.setQueueNames(CONNECTION_QUEUE);
        accountServiceListener.setConnectionFactory(springConnectionFactory);
        return accountServiceListener;
    }

}
