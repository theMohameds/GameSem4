package io.group9.player.plugins;

import com.badlogic.ashley.core.Engine;
import io.group9.player.plugins.PlayerFactory;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerFactoryTest {
    private Engine engine;
    private IPlayerService playerService;

    @BeforeEach
    void setUp() throws Exception {
        engine = new Engine();
        playerService = mock(IPlayerService.class);
        Field instField = PlayerServiceLocator.class.getDeclaredField("instance");
        instField.setAccessible(true);
        instField.set(null, playerService);
    }

    @Test
    void spawn_addsEntityWithPlayerComponent_andSetsService() {
        assertEquals(0, engine.getEntities().size());
        PlayerFactory.spawn(engine);
        assertNotEquals(0, engine.getEntities().size());
        PlayerComponent pc = engine.getEntities().first().getComponent(PlayerComponent.class);
        assertNotNull(pc);
        verify(playerService).setPlayerBody(pc.body);
        verify(playerService).setPlayerEntity(engine.getEntities().first());
        verify(playerService).setHealth(pc.health);
        verify(playerService).setMaxHealth(pc.maxHealth);
    }
}
