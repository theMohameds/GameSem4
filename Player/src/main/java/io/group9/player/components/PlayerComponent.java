package io.group9.player.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.graphics.Color;
import services.player.IPlayerService;


public class PlayerComponent implements Component {
    // Physics & identity
    public Body body;
    public Entity entity;
    public Color color = Color.WHITE;
    public boolean facingLeft = false;
    public Fixture attackSensorFixture;

    // Movement
    public float speed = 14f;
    public int maxJumps = 2;
    public int jumpsLeft = maxJumps;

    // Health
    public int health = 100;
    public int maxHealth = 100;
    public boolean isHurt = false;
    public float hurtDuration = 0.264f;
    public float hurtTimer = 0f;

    // Attack
    public boolean attacking = false;
    public float attackDuration = 0.3f;
    public float attackTimer = 0f;
    public boolean attackRequested = false;

    // Block
    public boolean isBlocking = false;
    public float blockDuration = 1.5f;
    public float blockTimer = 0f;

    // Wall‐jump etc.
    public boolean wallHanging = false;
    public float wallHangCooldownTimer = 0f;
    public float wallHangCooldownDuration = 1f;
    public float wallHangingDuration = 2f;
    public float wallHangingTimer = 0f;
    public boolean wallOnLeft = false;

    // Freeze logic used by RoundManager
    public boolean needsFreeze = false;

    public enum State {
        IDLE, RUN, JUMP, AIRSPIN,
        LIGHT_ATTACK, HEAVY_ATTACK, DEAD,
        DASH, BLOCK, LAND_WALL, HURT,
        SWORD_ATTACK, SWORD_IDLE, SWORD_RUN
    }
    public State state = State.IDLE;

    public enum AttackType { LIGHT, HEAVY }
    public AttackType attackType = AttackType.LIGHT;

    public static final float FIRST_JUMP_VELOCITY = 23f;
    public static final float DOUBLE_JUMP_VELOCITY = 23f;
}

