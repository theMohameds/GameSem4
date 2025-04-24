package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
            if (pc.body == null) continue;

            if (pc.state == PlayerComponent.State.HURT) {
                pc.hurtTimer -= deltaTime;
                if (pc.hurtTimer <= 0f) {
                    pc.state  = PlayerComponent.State.IDLE;
                    pc.isHurt = false;
                }
                continue;
            }


            if (pc.state == PlayerComponent.State.DEAD && pc.needsFreeze) {
                Vector2 vel = pc.body.getLinearVelocity();
                pc.body.setLinearVelocity(0f, vel.y);
                pc.body.setGravityScale(FALL_MULTIPLIER);
                pc.body.setType(BodyDef.BodyType.DynamicBody);
                pc.needsFreeze = false;
            }

            if (pc.state == PlayerComponent.State.DEAD) {
                continue;
            }


            if (pc.wallHangCooldownTimer > 0f) {
                pc.wallHangCooldownTimer -= deltaTime;
                if (pc.wallHangCooldownTimer < 0f) {
                    pc.wallHangCooldownTimer = 0f;
                }
            }


            if (pc.wallHanging) {
                updateWallHanging(pc, deltaTime);
                continue;
            }

            updateNormalState(pc);
            applyGravityScaling(pc);
        }
    }

    // Update wall–hanging behavior.
    private void updateWallHanging(PlayerComponent pc, float deltaTime) {
        // Cancel if on cooldown or key released
        if (pc.wallHangCooldownTimer > 0f || !isPressingWall(pc)) {
            disableWallHang(pc);
            return;
        }
        // Jump off wall
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            jumpOffWall(pc);
            return;
        }

        // Continue hanging
        pc.wallHangingTimer += deltaTime;
        if (pc.wallHangingTimer < pc.wallHangingDuration) {
            freezePlayer(pc);
        } else {
            slideDown(pc);
        }

        pc.state = PlayerComponent.State.LAND_WALL;
    }

    // Check if player is holding into the wall
    private boolean isPressingWall(PlayerComponent pc) {
        return pc.wallOnLeft
            ? Gdx.input.isKeyPressed(Input.Keys.A)
            : Gdx.input.isKeyPressed(Input.Keys.D);
    }

    // Exit wall hang and reset jumps
    private void disableWallHang(PlayerComponent pc) {
        pc.wallHanging = false;
        pc.wallHangingTimer = 0f;
        //pc.jumpsLeft = pc.maxJumps;
    }

    // Freeze in place
    private void freezePlayer(PlayerComponent pc) {
        pc.body.setLinearVelocity(0f, 0f);
        pc.body.setGravityScale(0f);
    }

    // Slide down the wall slowly
    private void slideDown(PlayerComponent pc) {
        pc.body.setGravityScale(FALL_MULTIPLIER);
        Vector2 vel = pc.body.getLinearVelocity();
        pc.body.setLinearVelocity(0f, vel.y);
    }

    // Jump off wall
    private void jumpOffWall(PlayerComponent pc) {
        disableWallHang(pc);
        pc.wallHangCooldownTimer = pc.wallHangCooldownDuration;
        float jumpX = pc.wallOnLeft ? pc.speed : -pc.speed;
        pc.body.setLinearVelocity(jumpX, PlayerComponent.FIRST_JUMP_VELOCITY);
        pc.body.setGravityScale(UPWARD_GRAVITY_SCALE);
        pc.state = PlayerComponent.State.JUMP;
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
