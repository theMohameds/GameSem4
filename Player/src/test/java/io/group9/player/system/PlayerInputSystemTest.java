package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import io.group9.player.components.PlayerComponent;
import io.group9.player.system.PlayerInputSystem;
import util.CoreResources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PlayerInputSystemTest {

    private Engine engine;
    private Body body;
    private PlayerInputSystem system;

    @BeforeAll
    static void initBox2D() {
        // Load native Box2D; prevents NPEs on Body mocks
        Box2D.init();
    }

    @BeforeEach
    void setUp() {
        engine = mock(Engine.class);
        body   = mock(Body.class);
        system = new PlayerInputSystem();
    }

    private PlayerComponent makePlayer() {
        PlayerComponent pc = new PlayerComponent();
        pc.body      = body;
        pc.speed     = 10f;
        pc.jumpsLeft = pc.maxJumps = 2;
        pc.attacking = false;
        return pc;
    }

    private ImmutableArray<Entity> wrap(PlayerComponent pc) {
        Entity e = new Entity();
        e.add(pc);
        return new ImmutableArray<>(com.badlogic.gdx.utils.Array.with(e));
    }

    /** Override the static Gdx.input field. */
    private static void overrideGdxInput(Input mockInput) throws Exception {
        Field inputField = Gdx.class.getDeclaredField("input");
        inputField.setAccessible(true);
        inputField.set(null, mockInput);
    }

    /** Force CoreResources.isRoundFrozen() to return the given value, via its private flag. */
    private static void overrideRoundFrozen(boolean frozen) throws Exception {
        // Assumes CoreResources has: private static boolean roundFrozen;
        Field frozenField = CoreResources.class.getDeclaredField("roundFrozen");
        frozenField.setAccessible(true);
        frozenField.setBoolean(null, frozen);
    }

    @Test
    void testMoveLeft() throws Exception {
        // 1) ensure round is _not_ frozen
        overrideRoundFrozen(false);

        // 2) inject a mocked Input
        Input mockInput = mock(Input.class);
        overrideGdxInput(mockInput);
        when(mockInput.isKeyPressed(Input.Keys.A)).thenReturn(true);

        // 3) set up the engine to return our player
        PlayerComponent pc = makePlayer();
        when(engine.getEntitiesFor(any())).thenReturn(wrap(pc));
        when(body.getLinearVelocity()).thenReturn(new Vector2(0, 0));

        // 4) run the system
        system.addedToEngine(engine);
        system.update(1f);

        // 5) verify we moved left
        verify(body).setLinearVelocity(-10f, 0f);
        assertTrue(pc.facingLeft);
    }
}
