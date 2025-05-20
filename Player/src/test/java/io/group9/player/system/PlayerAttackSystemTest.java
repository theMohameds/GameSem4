package io.group9.player.system;

import com.badlogic.gdx.physics.box2d.Box2D;           // <<< import this
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import io.group9.player.components.PlayerComponent;
import io.group9.player.components.PlayerComponent.AttackType;
import io.group9.player.components.PlayerComponent.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerAttackSystemTest {
    @Mock Engine engine;
    @Mock Body body;
    @Mock Fixture fixture;

    PlayerAttackSystem system;

    @BeforeAll
    static void loadBox2DNatives() {
        Box2D.init();
    }
    @BeforeEach
    void setUp() {
        system = new PlayerAttackSystem();
    }

    private ImmutableArray<Entity> wrapInImmutableArray(PlayerComponent pc) {
        Entity e = new Entity();
        e.add(pc);
        Array<Entity> arr = new Array<>();
        arr.add(e);
        return new ImmutableArray<>(arr);
    }

    private PlayerComponent makePlayer(AttackType type) {
        PlayerComponent pc = new PlayerComponent();
        pc.attackType = type;
        pc.attackDuration = 2.0f;
        pc.attacking = false;
        pc.attackRequested = true;
        pc.facingLeft = false;
        pc.body = body;
        return pc;
    }

    @Test
    void testLightAttackCreatesSensor() {
        PlayerComponent pc = makePlayer(AttackType.LIGHT);
        ImmutableArray<Entity> ents = wrapInImmutableArray(pc);

        when(engine.getEntitiesFor(any())).thenReturn(ents);
        when(body.createFixture(any(FixtureDef.class))).thenReturn(fixture);

        system.addedToEngine(engine);
        system.update(0f);

        assertFalse(pc.attackRequested);
        assertTrue(pc.attacking);
        assertEquals(pc.attackDuration, pc.attackTimer, 1e-6);
        assertEquals(State.LIGHT_ATTACK, pc.state);

        assertSame(fixture, pc.attackSensorFixture);
        verify(body, times(1)).createFixture(any(FixtureDef.class));
        verify(fixture).setUserData("playerAttack");
    }

    @Test
    void testHeavyAttackCreatesSensor() {
        PlayerComponent pc = makePlayer(AttackType.HEAVY);
        ImmutableArray<Entity> ents = wrapInImmutableArray(pc);

        when(engine.getEntitiesFor(any())).thenReturn(ents);
        when(body.createFixture(any(FixtureDef.class))).thenReturn(fixture);

        system.addedToEngine(engine);
        system.update(0f);

        assertFalse(pc.attackRequested);
        assertTrue(pc.attacking);
        assertEquals(pc.attackDuration, pc.attackTimer, 1e-6);
        assertEquals(State.HEAVY_ATTACK, pc.state);

        verify(body).createFixture(any(FixtureDef.class));
        verify(fixture).setUserData("playerAttack");
    }

    @Test
    void testAttackEndsResetsToIdleWhenGrounded() {
        PlayerComponent pc = makePlayer(AttackType.LIGHT);
        pc.attacking = true;
        pc.attackTimer = 0.0f;
        pc.attackSensorFixture = fixture;
        pc.jumpsLeft = pc.maxJumps;
        pc.isBlocking = false;
        ImmutableArray<Entity> ents = wrapInImmutableArray(pc);

        when(engine.getEntitiesFor(any())).thenReturn(ents);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));

        system.addedToEngine(engine);
        system.update(0.1f);

        assertNull(pc.attackSensorFixture);
        verify(body).destroyFixture(fixture);

        assertFalse(pc.attacking);
        assertEquals(State.IDLE, pc.state);
    }

    @Test
    void testAttackEndsTransitionsToJumpWhenRising() {
        PlayerComponent pc = makePlayer(AttackType.LIGHT);
        pc.attacking = true;
        pc.attackTimer = -0.1f;
        pc.attackSensorFixture = fixture;
        pc.jumpsLeft = pc.maxJumps - 1;
        pc.isBlocking = false;
        ImmutableArray<Entity> ents = wrapInImmutableArray(pc);

        when(engine.getEntitiesFor(any())).thenReturn(ents);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 5f));

        system.addedToEngine(engine);
        system.update(0.2f);

        assertNull(pc.attackSensorFixture);
        verify(body).destroyFixture(fixture);

        assertFalse(pc.attacking);
        assertEquals(State.JUMP, pc.state);
    }

    @Test
    void testAttackEndsTransitionsToAirspinWhenFallingOrStationary() {
        PlayerComponent pc = makePlayer(AttackType.LIGHT);
        pc.attacking = true;
        pc.attackTimer = 0f;
        pc.attackSensorFixture = fixture;
        pc.jumpsLeft = pc.maxJumps - 1;
        pc.isBlocking = false;
        ImmutableArray<Entity> ents = wrapInImmutableArray(pc);

        when(engine.getEntitiesFor(any())).thenReturn(ents);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0.05f));

        system.addedToEngine(engine);
        system.update(0.05f);

        assertNull(pc.attackSensorFixture);
        verify(body).destroyFixture(fixture);

        assertFalse(pc.attacking);
        assertEquals(State.AIRSPIN, pc.state);
    }
}
