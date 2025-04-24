package io.group9.player.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class PlayerComponent implements Component {
    public float speed = 14f;
    public int maxJumps = 2;
    public int jumpsLeft = 2;
    public Fixture attackSensorFixture;

    public boolean attacking = false;
    public float attackDuration = 0.3f;
    public float attackTimer = 0f;
    public boolean attackRequested = false;

    public boolean facingLeft = false;

    public Body body;

    public static final float FIRST_JUMP_VELOCITY = 23f;
    public static final float DOUBLE_JUMP_VELOCITY = 23f;

    // Walls
   public boolean wallHanging = false;
   public float wallHangCooldownTimer = 0f;
   public float wallHangCooldownDuration = 1f; // Duration in seconds
   public float wallHangingDuration = 2f; // Duration in seconds
   public float wallHangingTimer = 0f;
   public boolean wallOnLeft = false; //
    public boolean needsFreeze = false;

    public enum State {
        IDLE,
        RUN,
        JUMP,
        AIRSPIN,
        LIGHT_ATTACK,
        HEAVY_ATTACK,
        DEAD,
        DASH,
        BLOCK,
        LAND_WALL,
        HURT
    }

    // Combat
    public int   health         = 100;
    public float hurtDuration   = 0.264f;
    public float hurtTimer      = 0f;
    public boolean isHurt       = false;

    public State state = State.IDLE;
}

