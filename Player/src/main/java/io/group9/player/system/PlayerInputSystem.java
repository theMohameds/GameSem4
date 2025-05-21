package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import data.util.CoreResources;
import io.group9.player.components.PlayerComponent;

public class PlayerInputSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(
            Family.all(PlayerComponent.class).get()
        );
    }

    @Override
    public void update(float dt) {
        if (CoreResources.isRoundFrozen()) return;

        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;
            if (pc.state == PlayerComponent.State.DEAD ||
                pc.state == PlayerComponent.State.HURT) continue;

            // Movement
            Vector2 vel = pc.body.getLinearVelocity();
            float horiz = 0f;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                horiz         = -pc.speed;
                pc.facingLeft = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                horiz         =  pc.speed;
                pc.facingLeft = false;
            }
            pc.body.setLinearVelocity(horiz, vel.y);

            // Jump
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && pc.jumpsLeft > 0) {
                float vy = pc.jumpsLeft == pc.maxJumps
                    ? PlayerComponent.FIRST_JUMP_VELOCITY
                    : PlayerComponent.DOUBLE_JUMP_VELOCITY;
                pc.body.setLinearVelocity(vel.x, vy);
                pc.state = pc.jumpsLeft == pc.maxJumps
                    ? PlayerComponent.State.JUMP
                    : PlayerComponent.State.AIRSPIN;
                pc.jumpsLeft--;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.J) && !pc.attacking) {
                pc.attackRequested = true;
                pc.attackType = PlayerComponent.AttackType.LIGHT;
            }


            if (Gdx.input.isKeyJustPressed(Input.Keys.K) && !pc.attacking) {
                pc.attackRequested = true;
                pc.attackType = PlayerComponent.AttackType.HEAVY;
            }


            if (Gdx.input.isKeyPressed(Input.Keys.L)) {
                pc.isBlocking = true;
                pc.body.setLinearVelocity(0, pc.body.getLinearVelocity().y);
                pc.state = PlayerComponent.State.BLOCK;
            } else {
                pc.isBlocking = false;
            }
        }
    }
}
