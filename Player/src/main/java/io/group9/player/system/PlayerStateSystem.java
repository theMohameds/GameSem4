package io.group9.player.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import io.group9.player.components.PlayerComponent;

public class PlayerStateSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    // Use these gravity scales for ascending and falling.
    private static final float UPWARD_GRAVITY_SCALE = 5.5f;
    private static final float FALL_MULTIPLIER = 5.5f;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;


            // === KORRIGERET: Blokeringslogik placeret INDE i løkken === //
            if (pc.isBlocking && pc.jumpsLeft == pc.maxJumps) {
                pc.state = PlayerComponent.State.BLOCK;
                pc.blockTimer = pc.blockDuration;
                continue; // Spring over andre tilstandsændringer
            }

            // Update state if not attacking.
            if (!pc.attacking) {
                if (pc.jumpsLeft == pc.maxJumps) {
                    // On the ground.
                    if (Math.abs(pc.body.getLinearVelocity().x) > 0.1f)
                        pc.state = PlayerComponent.State.RUN;
                    else
                        pc.state = PlayerComponent.State.IDLE;
                } else {
                    // In air.
                    if (pc.body.getLinearVelocity().y > 0.1f)
                        pc.state = PlayerComponent.State.JUMP;
                    else
                        pc.state = PlayerComponent.State.AIRSPIN;
                }
            }

            // Adjust gravity scale: while rising, use UPWARD_GRAVITY_SCALE; if falling, use FALL_MULTIPLIER.
            if (pc.body.getLinearVelocity().y > 0)
                pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
            else
                pc.body.setGravityScale(FALL_MULTIPLIER);
        }

    }
}
