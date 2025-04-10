package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import io.group9.player.components.PlayerComponent;

public class PlayerStateSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    // Gravity scales
    private static final float UPWARD_GRAVITY_SCALE = 5.5f;
    private static final float FALL_MULTIPLIER      = 5.5f;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;

            // If the player is "wallHanging"
            if (pc.wallHanging) {
                // Increase the hang timer:
                pc.wallHangingTimer += deltaTime;

                // First phase: fully pinned for, say, 3 seconds
                if (pc.wallHangingTimer < pc.wallHangingDuration) {
                    // Lock x and y velocity:
                    Vector2 vel = pc.body.getLinearVelocity();
                    vel.x = 0f;
                    vel.y = 0f;
                    pc.body.setLinearVelocity(vel);

                    // Zero out gravity so you don't slide down
                    pc.body.setGravityScale(0f);
                }
                // Second phase: start sliding
                else {
                    // For example, re-enable normal fall gravity:
                    pc.body.setGravityScale(FALL_MULTIPLIER);

                    // If you want to remain pinned horizontally but let them fall:
                    Vector2 vel = pc.body.getLinearVelocity();
                    vel.x = 0f;             // so they don't move left/right
                    pc.body.setLinearVelocity(vel);

                    // You could optionally switch to a "WALL_SLIDE" state
                    // or just remain in LAND_WALL
                }

                // Force the animation state to LAND_WALL
                pc.state = PlayerComponent.State.LAND_WALL;
            }
            else {
                // If not wall hanging, reset the timer so next time it starts from 0:
                pc.wallHangingTimer = 0f;

                // Then handle normal ground/air states (assuming you're not attacking):
                if (!pc.attacking) {
                    if (pc.jumpsLeft == pc.maxJumps) {
                        // On the ground
                        if (Math.abs(pc.body.getLinearVelocity().x) > 0.1f)
                            pc.state = PlayerComponent.State.RUN;
                        else
                            pc.state = PlayerComponent.State.IDLE;
                    } else {
                        // In the air
                        if (pc.body.getLinearVelocity().y > 0.1f)
                            pc.state = PlayerComponent.State.JUMP;
                        else
                            pc.state = PlayerComponent.State.AIRSPIN;
                    }
                }

                // Normal gravity logic for ascending vs falling:
                if (pc.body.getLinearVelocity().y > 0) {
                    pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
                } else {
                    pc.body.setGravityScale(FALL_MULTIPLIER);
                }
            }
        }
    }
}
