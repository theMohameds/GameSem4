package io.group9.player.system;


import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import io.group9.player.components.PlayerComponent;

public class PlayerSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    .
    private static final float FIRST_JUMP_VELOCITY = 15f;
    private static final float DOUBLE_JUMP_VELOCITY = 15f;


    private static final float UPWARD_GRAVITY_SCALE = 2.5f;
    private static final float FALL_MULTIPLIER = 3f;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        // Get all entities having PlayerComponent.
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;

            // Process horizontal movement.
            float horizontal = 0f;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                horizontal = -pc.speed;
                pc.facingLeft = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                horizontal = pc.speed;
                pc.facingLeft = false;
            }

            Vector2 vel = pc.body.getLinearVelocity();
            pc.body.setLinearVelocity(horizontal, vel.y);

            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && pc.jumpsLeft > 0) {
                if (pc.jumpsLeft == pc.maxJumps) {  // First jump.
                    pc.body.setLinearVelocity(vel.x, FIRST_JUMP_VELOCITY);
                    pc.state = PlayerComponent.State.JUMP;
                } else {  // Double jump.
                    pc.body.setLinearVelocity(vel.x, DOUBLE_JUMP_VELOCITY);
                    pc.state = PlayerComponent.State.AIRSPIN;
                }
                pc.jumpsLeft--;
            }

            .
            if (pc.jumpsLeft == pc.maxJumps) {
                if (Math.abs(horizontal) > 0.1f)
                    pc.state = PlayerComponent.State.RUN;
                else
                    pc.state = PlayerComponent.State.IDLE;
            } else {  // In air.
                if (pc.body.getLinearVelocity().y > 0.1f)
                    pc.state = PlayerComponent.State.JUMP;
                else
                    pc.state = PlayerComponent.State.AIRSPIN;
            }

            if (pc.body.getLinearVelocity().y > 0) {
                pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
            } else {
                pc.body.setGravityScale(FALL_MULTIPLIER);
            }
        }
    }
}


