// src/test/java/io/group9/player/components/PlayerComponentTest.java
package io.group9.player.components;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerComponentTest {
    private PlayerComponent pc;

    @BeforeEach
    void setUp() {
        pc = new PlayerComponent();
    }

    @Test
    void defaultsAreCorrect() {
        // physics & identity
        assertNull(pc.body,        "body should start null");
        assertNull(pc.entity,      "entity should start null");
        assertEquals(Color.WHITE, pc.color,    "default color");
        assertFalse(pc.facingLeft, "should not be facing left");

        // movement
        assertEquals(14f,        pc.speed,   1e-6, "speed");
        assertEquals(2,          pc.maxJumps,       "maxJumps");
        assertEquals(pc.maxJumps, pc.jumpsLeft,      "jumpsLeft");

        // health
        assertEquals(100, pc.health,    "health");
        assertEquals(100, pc.maxHealth, "maxHealth");
        assertFalse(pc.isHurt,          "not hurt by default");
        assertEquals(0.264f, pc.hurtDuration, 1e-6, "hurtDuration");
        assertEquals(0f,     pc.hurtTimer,    1e-6, "hurtTimer");

        // attack
        assertFalse(pc.attacking,        "not attacking");
        assertEquals(0.3f, pc.attackDuration, 1e-6, "attackDuration");
        assertEquals(0f,   pc.attackTimer,    1e-6, "attackTimer");
        assertFalse(pc.attackRequested,  "no attack requested");

        // block
        assertFalse(pc.isBlocking,       "not blocking");
        assertEquals(1.5f, pc.blockDuration, 1e-6, "blockDuration");
        assertEquals(0f,   pc.blockTimer,    1e-6, "blockTimer");

        // wall
        assertFalse(pc.wallHanging,           "not wall-hanging");
        assertEquals(0f,   pc.wallHangCooldownTimer, 1e-6);
        assertEquals(1f,   pc.wallHangCooldownDuration, 1e-6);
        assertEquals(2f,   pc.wallHangingDuration,     1e-6);
        assertEquals(0f,   pc.wallHangingTimer,        1e-6);
        assertFalse(pc.wallOnLeft,         "no wall on left");

        // freeze
        assertFalse(pc.needsFreeze, "no freeze pending");

        // state & attackType
        assertEquals(PlayerComponent.State.IDLE,         pc.state,      "initial state");
        assertEquals(PlayerComponent.AttackType.LIGHT,   pc.attackType, "initial attack type");

        // jump constants
        assertEquals(23f, PlayerComponent.FIRST_JUMP_VELOCITY, 1e-6);
        assertEquals(23f, PlayerComponent.DOUBLE_JUMP_VELOCITY, 1e-6);
    }
}
