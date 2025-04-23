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

    // Gravity scale constants.
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

            // Decrement wall–hang cooldown timer if active.
            if (pc.wallHangCooldownTimer > 0f) {
                pc.wallHangCooldownTimer -= deltaTime;
                if (pc.wallHangCooldownTimer < 0f) {
                    pc.wallHangCooldownTimer = 0f;
                }
            }

            // Handle wall hanging logic.
            if (pc.wallHanging) {
                updateWallHanging(pc, deltaTime);
                // Since wall hanging handles its own state update, skip the normal logic.
                continue;
            }

            updateNormalState(pc);
            applyGravityScaling(pc);
        }
    }

    // Update wall–hanging behavior.
    private void updateWallHanging(PlayerComponent pc, float deltaTime) {
        if (pc.wallHangCooldownTimer > 0f) {  // If cooldown is active, exit wall hang.
            disableWallHang(pc);
            return;
        }
        if (!isPressingWall(pc)) { // Check if player is still pressing the wall.
            disableWallHang(pc);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) { // Jump off wall.
            jumpOffWall(pc);
            return;
        }
        pc.wallHangingTimer += deltaTime;  // Increment the wall-hanging timer.

        if (pc.wallHangingTimer < pc.wallHangingDuration) {
            freezePlayer(pc);
        } else {
            slideDown(pc);
        }

        pc.state = PlayerComponent.State.LAND_WALL;
    }

    // Check if the player is holding the key toward the wall.
    private boolean isPressingWall(PlayerComponent pc) {
        return (pc.wallOnLeft && Gdx.input.isKeyPressed(Input.Keys.A)) ||
            (!pc.wallOnLeft && Gdx.input.isKeyPressed(Input.Keys.D));
    }

    // Disable wall hang and reset related state.
    private void disableWallHang(PlayerComponent pc) {
        pc.wallHanging = false;
        pc.wallHangingTimer = 0f;
        //pc.jumpsLeft = pc.maxJumps;
    }

    // Freeze the player's movement while wall hanging.
    private void freezePlayer(PlayerComponent pc) {
        Vector2 vel = pc.body.getLinearVelocity();
        vel.setZero();
        pc.body.setLinearVelocity(vel);
        pc.body.setGravityScale(0f);
    }

    // Let the player slide down from the wall.
    private void slideDown(PlayerComponent pc) {
        pc.body.setGravityScale(FALL_MULTIPLIER);
        Vector2 vel = pc.body.getLinearVelocity();
        vel.x = 0f;
        pc.body.setLinearVelocity(vel);
    }

    // Execute wall jump: leave wall hang and apply jump impulse.
    private void jumpOffWall(PlayerComponent pc) {
        disableWallHang(pc);
        pc.wallHangCooldownTimer = pc.wallHangCooldownDuration;
        float jumpSpeedX = pc.wallOnLeft ? pc.speed : -pc.speed;
        pc.body.setLinearVelocity(jumpSpeedX, PlayerComponent.FIRST_JUMP_VELOCITY);
        pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
    }

    // Normal state update when not wall hanging.
    private void updateNormalState(PlayerComponent pc) {
        boolean onGround = Math.abs(pc.body.getLinearVelocity().y) < 0.01f;

        if (!pc.attacking) {
            if (onGround) {
                System.out.println(pc.body.getLinearVelocity().x);
                // On-ground: decide between RUN and IDLE.
                PlayerComponent.State newState;
                float absVelX = Math.abs(pc.body.getLinearVelocity().x);

                if(absVelX > 14.5f){
                    newState = PlayerComponent.State.DASH;
                }else if (absVelX > 0.1f && absVelX < 14.5f) {
                    newState = PlayerComponent.State.RUN;
                } else {
                    newState = PlayerComponent.State.IDLE;
                }

                if (pc.state != newState) {
                    pc.state = newState;
                }

            } else {
                // In-air: choose between jump and air spin.
                PlayerComponent.State newState;
                if(pc.jumpsLeft == 0){
                    newState = PlayerComponent.State.AIRSPIN;
                }else {
                    newState = PlayerComponent.State.JUMP;
                }

                if (pc.state != newState) {
                    pc.state = newState;
                }
            }
        }
    }

    // Adjust gravity based on player's vertical movement.
    private void applyGravityScaling(PlayerComponent pc) {
        if (pc.body.getLinearVelocity().y > 0) {
            pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
        } else {
            pc.body.setGravityScale(FALL_MULTIPLIER);
        }
    }
}
