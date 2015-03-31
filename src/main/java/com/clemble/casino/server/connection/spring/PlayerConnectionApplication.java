package com.clemble.casino.server.connection.spring;

import com.clemble.casino.server.spring.WebBootSpringConfiguration;
import com.clemble.casino.server.spring.common.ClembleBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by mavarazy on 3/31/15.
 */
@Import({WebBootSpringConfiguration.class, PlayerConnectionSpringConfiguration.class})
public class PlayerConnectionApplication implements ClembleBootApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(PlayerConnectionApplication.class, args);
    }

}
