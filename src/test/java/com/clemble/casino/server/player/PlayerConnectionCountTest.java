package com.clemble.casino.server.player;

import com.clemble.casino.player.PlayerProfile;
import com.clemble.casino.server.connection.service.ServerPlayerConnectionService;
import com.clemble.casino.server.connection.spring.PlayerConnectionSpringConfiguration;
import com.clemble.test.random.ObjectGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author Anton Oparin (antono@clemble.com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = PlayerConnectionSpringConfiguration.class)
public class PlayerConnectionCountTest {

    @Autowired
    public ServerPlayerConnectionService connectionService;

    @Test
    public void testConnectionCount() {
        PlayerProfile A = ObjectGenerator.generate(PlayerProfile.class);
        PlayerProfile B = ObjectGenerator.generate(PlayerProfile.class);

        connectionService.create(A);
        connectionService.create(B);

        Assert.assertEquals(connectionService.getConnectionsCount(A.getPlayer()).intValue(), 0);
        Assert.assertEquals(connectionService.getConnectionsCount(A.getPlayer()).intValue(), 0);

        connectionService.connect(A.getPlayer(), B.getPlayer());

        Assert.assertEquals(connectionService.getConnectionsCount(A.getPlayer()).intValue(), 1);
        Assert.assertEquals(connectionService.getConnectionsCount(A.getPlayer()).intValue(), 1);
    }
}
