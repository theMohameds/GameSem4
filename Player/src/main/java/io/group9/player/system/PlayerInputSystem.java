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
        // Process all entities that have a PlayerComponent.
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;

            // --- Horizontal Movement ---
            float horizontal = 0f;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                horizontal = -pc.speed;
                pc.facingLeft = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                horizontal = pc.speed;
                pc.facingLeft = false;
            }
            // Preserve current vertical velocity.
            Vector2 vel = pc.body.getLinearVelocity();
            pc.body.setLinearVelocity(horizontal, vel.y);

            // --- Jump Input ---
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && pc.jumpsLeft > 0) {
                if (pc.jumpsLeft == pc.maxJumps) { // First jump.
                    pc.body.setLinearVelocity(vel.x, PlayerComponent.FIRST_JUMP_VELOCITY);
                    pc.state = PlayerComponent.State.JUMP;
                } else { // Double jump.
                    pc.body.setLinearVelocity(vel.x, PlayerComponent.DOUBLE_JUMP_VELOCITY);
                    pc.state = PlayerComponent.State.AIRSPIN;
                }
                pc.jumpsLeft--;
            }

            // --- Attack Input ---
            // If attack key is pressed and not already attacking, set attackRequested flag.
            if (Gdx.input.isKeyJustPressed(Input.Keys.J) && !pc.attacking) {
                pc.attackRequested = true;

                pc.isBlocking = Gdx.input.isKeyPressed(Input.Keys.L); // Hold L nede for at blokere

                if (pc.isBlocking) {
                    pc.body.setLinearVelocity(0, pc.body.getLinearVelocity().y);
                }
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                pc.attacking = true;
                pc.state = PlayerComponent.State.BLOCK;

            }
        }
    }
}
