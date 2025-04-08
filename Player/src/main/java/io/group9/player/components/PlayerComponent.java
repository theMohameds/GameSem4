package io.group9.player.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

public class PlayerComponent implements Component {
    public float speed = 10f;
    public int maxJumps = 2;
    public int jumpsLeft = 2;

    public boolean facingLeft = false;

    public Body body;

    public enum State { IDLE, RUN, JUMP, AIRSPIN }
    public State state = State.IDLE;
}

