package io.group9.player.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import components.CollisionCategories;
import io.group9.player.components.PlayerComponent;

public class PlayerAttackSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        // Process all entities with PlayerComponent.
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null)
                continue;

            // Check if an attack has been requested by the input system.
            if (pc.attackRequested && !pc.attacking) {
                // Begin attack:
                pc.attacking = true;
                pc.attackTimer = pc.attackDuration;
                // Set state to attack (choose LIGHT_ATTACK or HEAVY_ATTACK as needed).
                pc.state = PlayerComponent.State.LIGHT_ATTACK;
                // Reset the attack request flag.
                pc.attackRequested = false;
                createAttackSensor(pc);
            }

            // If attack is active, count down the timer.
            if (pc.attacking) {
                pc.attackTimer -= deltaTime;
                if (pc.attackTimer <= 0) {
                    removeAttackSensor(pc);
                    pc.attacking = false;
                    // Reset state based on whether grounded.
                    if (pc.jumpsLeft == pc.maxJumps)
                        pc.state = PlayerComponent.State.IDLE;
                    else {
                        if (pc.body.getLinearVelocity().y > 0.1f)
                            pc.state = PlayerComponent.State.JUMP;
                        else
                            pc.state = PlayerComponent.State.AIRSPIN;


                    }
                    if (pc.isBlocking) {
                        pc.attackRequested = false;
                        return;
                    }

                }
            }
        }
    }

    // Create an attack sensor fixture on the player's body.
    private void createAttackSensor(PlayerComponent pc) {
        // Define sensor dimensions (world units).
        float sensorWidth = 16f / CoreResources.PPM;  // Example: 8 pixels.
        float sensorHeight = 30f / CoreResources.PPM;  // Example: 15 pixels.
        // Calculate offset from player's center.
        float offsetX = pc.facingLeft
            ? -((20f / CoreResources.PPM) / 2 + sensorWidth / 2)
            : ((20f / CoreResources.PPM) / 2 + sensorWidth / 2);

        float offsetY = 0f;
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(sensorWidth / 2, sensorHeight / 2, new com.badlogic.gdx.math.Vector2(offsetX, offsetY), 0);

        FixtureDef sensorFD = new FixtureDef();
        sensorFD.shape = sensorShape;
        sensorFD.isSensor = true;
        // Set collision filtering: category = ATTACK, mask = ENEMY.
        sensorFD.filter.categoryBits = CollisionCategories.ATTACK;
        sensorFD.filter.maskBits = CollisionCategories.ENEMY_HURTBOX;

        Fixture sensorFixture = pc.body.createFixture(sensorFD);
        // Tag this sensor fixture for contact processing.
        sensorFixture.setUserData("playerAttack");
        pc.attackSensorFixture = sensorFixture;

        sensorShape.dispose();
    }

    private void removeAttackSensor(PlayerComponent pc) {
        if (pc.attackSensorFixture != null) {
            pc.body.destroyFixture(pc.attackSensorFixture);
            pc.attackSensorFixture = null;
        }
    }
}
