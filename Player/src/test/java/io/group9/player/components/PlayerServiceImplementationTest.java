// src/test/java/io/group9/player/components/PlayerServiceImplementationTest.java
package io.group9.player.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.CoreResources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceImplementationTest {
    private PlayerServiceImplementation service;
    private Entity               ent;
    private PlayerComponent      comp;
    private Body                 body;

    @BeforeEach
    void setUp() {
        service = new PlayerServiceImplementation();
        ent     = new Entity();
        comp    = new PlayerComponent();
        body    = mock(Body.class);

        // Wire up entity↔component and service
        comp.body = body;
        ent.add(comp);
        service.setPlayerEntity(ent);
        service.setPlayerBody(body);
    }

    @Test
    void testGettersAndSetters() {
        service.setHealth(42);
        assertEquals(42, service.getHealth());

        service.setMaxHealth(77);
        assertEquals(77, service.getMaxHealth());

        assertSame(ent,  service.getPlayerEntity());
        assertSame(body, service.getPlayerBody());
    }

    @Test
    void resetForRound_initializesComponentAndBody() {
        // Make fields non-default to catch the reset
        comp.health   = 1;
        comp.jumpsLeft = 0;
        comp.attacking = true;
        comp.attackRequested = true;
        comp.isHurt    = true;
        comp.hurtTimer = 5f;
        comp.attackTimer = 2f;
        comp.blockTimer  = 3f;
        comp.wallHanging = true;
        comp.wallHangingTimer = 4f;
        comp.wallHangCooldownTimer = 9f;
        comp.state     = PlayerComponent.State.HEAVY_ATTACK;
        comp.facingLeft = true;

        // Call under test
        Vector2 spawn = new Vector2(64, 128);
        service.resetForRound(spawn);

        // Component fields
        assertEquals(100, service.getHealth());
        assertEquals(comp.maxHealth, comp.health);
        assertEquals(comp.maxJumps,  comp.jumpsLeft);
        assertFalse(comp.attacking);
        assertFalse(comp.attackRequested);
        assertFalse(comp.isHurt);
        assertEquals(0f, comp.hurtTimer, 1e-6);
        assertEquals(0f, comp.attackTimer, 1e-6);
        assertEquals(0f, comp.blockTimer,  1e-6);
        assertFalse(comp.wallHanging);
        assertEquals(0f, comp.wallHangingTimer, 1e-6);
        assertEquals(0f, comp.wallHangCooldownTimer, 1e-6);
        assertEquals(PlayerComponent.State.IDLE, comp.state);
        assertFalse(comp.facingLeft);

        // Body interactions
        float expectX = spawn.x / CoreResources.PPM;
        float expectY = spawn.y / CoreResources.PPM;
        verify(body).setType(any());                 // BodyDef.BodyType.DynamicBody
        verify(body).setGravityScale(1f);
        verify(body).setLinearVelocity(0f, 0f);
        verify(body).setAngularVelocity(0f);
        verify(body).setSleepingAllowed(false);
        verify(body).setAwake(true);
        verify(body).setTransform(expectX, expectY, 0f);
    }

    @Test
    void freezeForRound_setsNeedsFreeze() {
        assertFalse(comp.needsFreeze);
        service.freezeForRound();
        assertTrue(comp.needsFreeze);
    }
}
