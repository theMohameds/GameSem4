package io.group9.player.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import io.group9.player.components.PlayerComponent;

public class PlayerInputSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            // ❌ skip all input if dead or hurt
            if (pc.state == PlayerComponent.State.DEAD ||
                pc.state == PlayerComponent.State.HURT) continue;
            if (pc.body == null) continue;

            // horizontal move
            float horizontal = 0f;
            if (!pc.wallHanging) {
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    horizontal = -pc.speed; pc.facingLeft = true;
                } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    horizontal = pc.speed;  pc.facingLeft = false;
                }
            }
            Vector2 vel = pc.body.getLinearVelocity();
            pc.body.setLinearVelocity(horizontal, vel.y);

            // jump
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && pc.jumpsLeft > 0) {
                if (pc.jumpsLeft == pc.maxJumps) {
                    pc.body.setLinearVelocity(vel.x, PlayerComponent.FIRST_JUMP_VELOCITY);
                    pc.state = PlayerComponent.State.JUMP;
                } else {
                    pc.body.setLinearVelocity(vel.x, PlayerComponent.DOUBLE_JUMP_VELOCITY);
                    pc.state = PlayerComponent.State.AIRSPIN;
                }
                pc.jumpsLeft--;
            }

            // attack
            if (Gdx.input.isKeyJustPressed(Input.Keys.J) && !pc.attacking) {
                pc.attackRequested = true;
            }
        }
    }
}
