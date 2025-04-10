package io.group9.enemy.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

public class EnemyComponent implements Component {
    public Body body;
    public boolean facingLeft = true;

    // Damage-related fields
    public int health = 100; // Starting health
    public float hurtDuration = 0.264f;
    public float hurtTimer = 0f;
    public boolean isHurt = false;

    // Extend the state to include DEAD.
    public enum State {
        IDLE,
        HURT,
        DEAD
    }

    public State state = State.IDLE;
}

