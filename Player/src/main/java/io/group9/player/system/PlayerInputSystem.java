package io.group9.player.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;

public class PlayerInputSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    @Override public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override public void update(float dt) {

        if (CoreResources.isRoundFrozen()) return;

        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;
            if (pc.state == PlayerComponent.State.DEAD ||
                pc.state == PlayerComponent.State.HURT) continue;

            float horizontal = 0f;
            if (!pc.wallHanging) {
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    horizontal = -pc.speed; pc.facingLeft = true;
                } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    horizontal =  pc.speed; pc.facingLeft = false;
                }
            }
            Vector2 vel = pc.body.getLinearVelocity();
            pc.body.setLinearVelocity(horizontal, vel.y);

            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && pc.jumpsLeft > 0) {
                float vy = (pc.jumpsLeft == pc.maxJumps)
                    ? PlayerComponent.FIRST_JUMP_VELOCITY
                    : PlayerComponent.DOUBLE_JUMP_VELOCITY;
                pc.body.setLinearVelocity(vel.x, vy);
                pc.state = (pc.jumpsLeft == pc.maxJumps)
                    ? PlayerComponent.State.JUMP
                    : PlayerComponent.State.AIRSPIN;
                pc.jumpsLeft--;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.J) && !pc.attacking) {
                pc.attackRequested = true;
            }
        }
    }
}
