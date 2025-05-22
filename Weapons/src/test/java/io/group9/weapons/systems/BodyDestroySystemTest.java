package io.group9.weapons.systems;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import data.WorldProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BodyDestroySystemTest {

    @BeforeEach
    void clearQueue() throws Exception {
        // Use reflection to clear static queue before each test
        Field field = BodyDestroySystem.class.getDeclaredField("toDestroy");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Body> queue = (List<Body>) field.get(null);
        queue.clear();
    }

    @Test
    void schedule_AddsBodiesToQueue() throws Exception {
        Body b1 = mock(Body.class);
        Body b2 = mock(Body.class);

        BodyDestroySystem.schedule(b1);
        BodyDestroySystem.schedule(b2);

        Field field = BodyDestroySystem.class.getDeclaredField("toDestroy");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Body> queue = (List<Body>) field.get(null);

        assertEquals(2, queue.size(), "Queue should contain two bodies");
        assertTrue(queue.containsAll(Arrays.asList(b1, b2)), "Queue should contain scheduled bodies");
    }

    @Test
    void update_DestroyScheduledBodies() throws Exception {
        Body b1 = mock(Body.class);
        Body b2 = mock(Body.class);
        World mockWorld = mock(World.class);

        try (MockedStatic<WorldProvider> wp = Mockito.mockStatic(WorldProvider.class)) {
            wp.when(WorldProvider::getWorld).thenReturn(mockWorld);

            BodyDestroySystem.schedule(b1);
            BodyDestroySystem.schedule(b2);

            new BodyDestroySystem().update(0f);

            // Verify destroyBody called on each
            verify(mockWorld).destroyBody(b1);
            verify(mockWorld).destroyBody(b2);

            // Subsequent update should not call again
            clearInvokingQueue();
            new BodyDestroySystem().update(0f);

            verifyNoMoreInteractions(mockWorld);
        }
    }

    // Helper to use reflection to clear queue between update calls
    private void clearInvokingQueue() throws Exception {
        Field field = BodyDestroySystem.class.getDeclaredField("toDestroy");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Body> queue = (List<Body>) field.get(null);
        queue.clear();
    }
}
