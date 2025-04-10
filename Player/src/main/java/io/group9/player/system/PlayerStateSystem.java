package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
            if (pc.body == null) {
                continue;
            }

            // Decrement wall-hang cooldown if active
            if (pc.wallHangCooldownTimer > 0f) {
                pc.wallHangCooldownTimer -= deltaTime;
                if (pc.wallHangCooldownTimer < 0f) {
                    pc.wallHangCooldownTimer = 0f;
                }
            }

            // ---------------- wall logic ----------------
            if (pc.wallHanging) {
               // pc.jumpsLeft = pc.maxJumps;

                if (pc.wallHangCooldownTimer > 0f) {
                    pc.wallHanging = false;
                    pc.wallHangingTimer = 0f;
                } else {

                    boolean pressingLeftWall  = pc.wallOnLeft  && Gdx.input.isKeyPressed(Input.Keys.A);
                    boolean pressingRightWall = !pc.wallOnLeft && Gdx.input.isKeyPressed(Input.Keys.D);
                    boolean pressingWall = pressingLeftWall || pressingRightWall;

                    if (!pressingWall) {
                        pc.wallHanging = false;
                        pc.wallHangingTimer = 0f;
                    }

                    // Jump off wall
                    else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                        pc.wallHanging = false;
                        pc.wallHangingTimer = 0f;
                        pc.wallHangCooldownTimer = pc.wallHangCooldownDuration;

                        float jumpSpeedX = pc.wallOnLeft ? pc.speed : -pc.speed;
                        pc.body.setLinearVelocity(jumpSpeedX, PlayerComponent.FIRST_JUMP_VELOCITY);
                        pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
                    }

                    else {
                        // Wall-hanging timer
                        pc.wallHangingTimer += deltaTime;
                        if (pc.wallHangingTimer < pc.wallHangingDuration) {
                            // Freeze
                            Vector2 vel = pc.body.getLinearVelocity();
                            vel.setZero();
                            pc.body.setLinearVelocity(vel);
                            pc.body.setGravityScale(0f);
                        } else {
                            // Slide down
                            pc.body.setGravityScale(FALL_MULTIPLIER);
                            Vector2 vel = pc.body.getLinearVelocity();
                            vel.x = 0f;
                            pc.body.setLinearVelocity(vel);
                        }
                        pc.state = PlayerComponent.State.LAND_WALL;
                        continue;
                    }
                }
            }

            // ---------------- normal logic ----------------
            if (!pc.attacking) {
                // If the player is on the ground (jumpsLeft == maxJumps),
                // choose RUN vs IDLE based on horizontal speed
                if (pc.jumpsLeft == pc.maxJumps) {
                    if (Math.abs(pc.body.getLinearVelocity().x) > 0.1f) {
                        pc.state = PlayerComponent.State.RUN;
                    } else {
                        pc.state = PlayerComponent.State.IDLE;
                    }
                } else {
                    // In the air
                    if (pc.body.getLinearVelocity().y > 0.1f) {
                        pc.state = PlayerComponent.State.JUMP;
                    } else {
                        pc.state = PlayerComponent.State.AIRSPIN;
                    }
                }
            }

            // Apply ascending/falling gravity
            if (pc.body.getLinearVelocity().y > 0) {
                pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
            } else {
                pc.body.setGravityScale(FALL_MULTIPLIER);
            }
        }
    }
}
