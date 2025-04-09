package io.group9.player.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

public class PlayerComponent implements Component {
    public float speed = 10f;
    public int maxJumps = 2;
    public int jumpsLeft = 2;

    public boolean facingLeft = false;

    public Body body;

    // Walls
    public boolean isWallBound = false;
    public float wallBoundDuration = 3.0f; // Duration in seconds
    public float wallBoundTimer = 0f;

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
        WALL_LAND
    }

    public State state = State.IDLE;
}

