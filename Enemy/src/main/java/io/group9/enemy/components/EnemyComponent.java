package io.group9.enemy.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

public class EnemyComponent implements Component {
    public Body body;
    public boolean facingLeft = true;

    public float hurtDuration = 0.264f; // 4 * 0.066f
    public float hurtTimer = 0f;
    public boolean isHurt = false;

    public enum State {
        IDLE,
        HURT
    }

    public State state = State.IDLE;
}
