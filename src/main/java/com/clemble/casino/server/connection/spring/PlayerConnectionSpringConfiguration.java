package com.clemble.casino.server.connection.spring;

import com.clemble.casino.player.service.PlayerConnectionService;
import com.clemble.casino.server.connection.controller.PlayerFriendInvitationServiceController;
import com.clemble.casino.server.connection.listener.PlayerDiscoveryNotificationEventListener;
import com.clemble.casino.server.connection.listener.PlayerGraphCreationListener;
import com.clemble.casino.server.connection.listener.PlayerGraphPopulatorListener;
import com.clemble.casino.server.connection.repository.PlayerFriendInvitationRepository;
import com.clemble.casino.server.connection.service.PlayerGraphService;
import com.clemble.casino.server.player.notification.ServerNotificationService;
import com.clemble.casino.server.player.notification.SystemNotificationService;
import com.clemble.casino.server.player.notification.SystemNotificationServiceListener;
import com.clemble.casino.server.spring.common.CommonSpringConfiguration;
import com.clemble.casino.server.connection.controller.PlayerConnectionServiceController;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.util.HashMap;

import static com.clemble.casino.server.spring.common.ConnectionClientSpringConfiguration.Default.*;

@Configuration
@Import({ CommonSpringConfiguration.class, ServerPlayerConnectionsSpringConfiguration.MongoPlayerConnectionsSpringConfiguration.class })
public class PlayerConnectionSpringConfiguration {

    @Bean
    public PlayerGraphCreationListener playerConnectionNetworkCreationListener(PlayerGraphService playerRepository, SystemNotificationServiceListener notificationServiceListener) {
        PlayerGraphCreationListener networkCreationService = new PlayerGraphCreationListener(playerRepository);
        notificationServiceListener.subscribe(networkCreationService);
        return networkCreationService;
    }

    @Bean
    public PlayerGraphPopulatorListener socialNetworkConnectionCreatorListener(PlayerGraphService playerRepository, SystemNotificationService notificationService, SystemNotificationServiceListener notificationServiceListener) {
        PlayerGraphPopulatorListener connectionCreatorListener = new PlayerGraphPopulatorListener(playerRepository, notificationService);
        notificationServiceListener.subscribe(connectionCreatorListener);
        return connectionCreatorListener;
    }

    @Bean
    public PlayerDiscoveryNotificationEventListener playerDiscoveryNotifierEventListener(
        @Qualifier("playerNotificationService") ServerNotificationService notificationService,
        SystemNotificationServiceListener notificationServiceListener) {
        PlayerDiscoveryNotificationEventListener discoveryNotifierEventListener = new PlayerDiscoveryNotificationEventListener(notificationService);
        notificationServiceListener.subscribe(discoveryNotifierEventListener);
        return discoveryNotifierEventListener;
    }

    @Bean
    public PlayerConnectionServiceController playerConnectionController(PlayerGraphService connectionService) {
        return new PlayerConnectionServiceController(connectionService);
    }

    @Bean
    public PlayerFriendInvitationServiceController playerFriendInvitationServiceController(
        PlayerGraphService graphService,
        @Qualifier("playerNotificationService") ServerNotificationService notificationService,
        PlayerFriendInvitationRepository invitationRepository) {
        return new PlayerFriendInvitationServiceController(invitationRepository, notificationService, graphService);
    }

       @Bean
    public SimpleMessageListenerContainer connectionServiceListener(
        PlayerConnectionServiceController playerConnectionController,
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
