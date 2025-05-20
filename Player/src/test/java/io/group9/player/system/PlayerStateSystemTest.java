package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import io.group9.player.components.PlayerComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerStateSystemTest {

    @Mock Engine engine;
    @Mock Body body;
    @Mock Input input;

    PlayerStateSystem system;

    @BeforeAll
    static void loadBox2DNatives() {
        Box2D.init();
    }

    @BeforeEach
    void setUp() {
        system = new PlayerStateSystem();
        Gdx.input = input;
    }

    private ImmutableArray<Entity> wrapInImmutableArray(PlayerComponent pc) {
        Entity e = new Entity();
        e.add(pc);
        Array<Entity> arr = new Array<>();
        arr.add(e);
        return new ImmutableArray<>(arr);
    }

    private PlayerComponent makePlayer() {
        PlayerComponent pc = new PlayerComponent();
        pc.body = body;
        pc.maxJumps = 2;
        pc.jumpsLeft = 2;
        pc.blockDuration = 3.0f;
        pc.wallHangCooldownDuration = 2.0f;
        pc.wallHangingDuration = 1.5f;
        return pc;
    }

    @Test
    void testHurtStateExpiresResetsToIdle() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.HURT;
        pc.hurtTimer = 1.0f;
        pc.isHurt = true;
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));

        system.addedToEngine(engine);
        system.update(1.0f);

        assertEquals(PlayerComponent.State.IDLE, pc.state);
        assertFalse(pc.isHurt);
    }

    @Test
    void testBlockingSetsBlockState() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.IDLE;
        pc.isBlocking = true;
        pc.jumpsLeft = pc.maxJumps;
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));

        system.addedToEngine(engine);
        system.update(0f);

        assertEquals(PlayerComponent.State.BLOCK, pc.state);
        assertEquals(pc.blockDuration, pc.blockTimer, 1e-6);
    }

    @Test
    void testDeadFreezesBodyAndClearsNeedsFreeze() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.DEAD;
        pc.needsFreeze = true;
        Vector2 vel = new Vector2(1f, 2f);
        when(body.getLinearVelocity()).thenReturn(vel);
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));

        system.addedToEngine(engine);
        system.update(0f);

        verify(body).setLinearVelocity(0f, vel.y);
        verify(body).setGravityScale(5.5f);
        verify(body).setType(BodyDef.BodyType.DynamicBody);
        assertFalse(pc.needsFreeze);
    }

    @Test
    void testDisableWallHangWhenNotPressingWall() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.IDLE;
        pc.wallHanging = true;
        pc.wallOnLeft = true;
        when(input.isKeyPressed(Input.Keys.A)).thenReturn(false);
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));

        system.addedToEngine(engine);
        system.update(0f);

        assertFalse(pc.wallHanging);
        assertEquals(0f, pc.wallHangingTimer, 1e-6);
    }

    @Test
    void testJumpOffWallOnWKey() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.IDLE;
        pc.wallHanging = true;
        pc.wallOnLeft = false;
        when(input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        when(input.isKeyJustPressed(Input.Keys.W)).thenReturn(true);
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));

        system.addedToEngine(engine);
        system.update(0f);

        assertFalse(pc.wallHanging);
        assertEquals(pc.wallHangCooldownDuration, pc.wallHangCooldownTimer, 1e-6);
        verify(body).setLinearVelocity(-pc.speed, PlayerComponent.FIRST_JUMP_VELOCITY);
        verify(body).setGravityScale(5.5f);
        assertEquals(PlayerComponent.State.JUMP, pc.state);
    }

    @Test
    void testUpdateNormalStateTransitions() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.IDLE;
        pc.attacking = false;
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));
        system.addedToEngine(engine);

        // Run
        when(body.getLinearVelocity()).thenReturn(new Vector2(5f, 0f));
        system.update(0f);
        assertEquals(PlayerComponent.State.RUN, pc.state);

        // Dash
        when(body.getLinearVelocity()).thenReturn(new Vector2(15f, 0f));
        system.update(0f);
        assertEquals(PlayerComponent.State.DASH, pc.state);

        // Jump
        pc.jumpsLeft = 1;
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 5f));
        system.update(0f);
        assertEquals(PlayerComponent.State.JUMP, pc.state);

        // Airspin
        pc.jumpsLeft = 0;
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 5f));
        system.update(0f);
        assertEquals(PlayerComponent.State.AIRSPIN, pc.state);
    }

    @Test
    void testApplyGravityScaling() {
        PlayerComponent pc = makePlayer();
        pc.state = PlayerComponent.State.IDLE;
        when(engine.getEntitiesFor(any())).thenReturn(wrapInImmutableArray(pc));
        system.addedToEngine(engine);

        // Upward
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 5f));
        system.update(0f);
        verify(body).setGravityScale(5.5f);

        // Downward
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, -5f));
        system.update(0f);
        verify(body, atLeastOnce()).setGravityScale(5.5f);
    }
}
