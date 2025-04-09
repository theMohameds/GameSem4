package io.group9.player.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.group9.common.WeaponType;

public class PlayerComponent implements Component {
    public WeaponType equippedWeapon;
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

    public boolean isWallBound = false;
    public float wallBoundDuration = 3.0f;
    public float wallBoundTimer = 0f;

    public enum State {
        IDLE, RUN, JUMP, AIRSPIN, LIGHT_ATTACK, HEAVY_ATTACK, DEAD, DASH, BLOCK, WALL_LAND
    }

    public State state = State.IDLE;
}
