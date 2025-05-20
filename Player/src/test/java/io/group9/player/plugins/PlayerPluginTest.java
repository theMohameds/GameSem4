package io.group9.player.plugins;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import contact.IContactDispatcherService;
import locators.ContactDispatcherLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerPluginTest {
    private PlayerPlugin plugin;
    private Engine engine;

    @BeforeEach
    void setUp() throws Exception {
        // 0) Mock out Gdx.app so Gdx.app.log(...) won't NPE
        Application mockApp = mock(Application.class);
        Gdx.app = mockApp;

        // 1) Inject a mock contact dispatcher before plugin ctor runs
        IContactDispatcherService mockDispatcher = mock(IContactDispatcherService.class);
        Field instField = ContactDispatcherLocator.class.getDeclaredField("INSTANCE");
        instField.setAccessible(true);
        instField.set(null, mockDispatcher);

        // 2) Now safe to construct plugin (its ctor calls ContactDispatcherLocator.get())
        engine = mock(Engine.class);
        plugin = new PlayerPlugin();
    }

    @Test
    void createEntities_spawnsOnlyOnce() {
        // Act
        plugin.createEntities(engine);
        plugin.createEntities(engine);

        // Assert: addEntity(...) only once
        verify(engine, times(1)).addEntity(any(Entity.class));
    }

    @Test
    void getPriority_returns2() {
        assertEquals(2, plugin.getPriority());
    }
}
