package io.group9.player.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import data.util.CoreResources;
import data.components.CollisionCategories;
import io.group9.player.components.PlayerComponent;

public class PlayerAttackSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(
            Family.all(PlayerComponent.class).get()
        );
    }

    @Override
    public void update(float deltaTime) {
        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (pc.body == null) continue;

            if (pc.attackRequested && !pc.attacking) {
                pc.attacking = true;
                pc.attackTimer = pc.attackDuration;

                if (pc.attackType == PlayerComponent.AttackType.HEAVY)
                    pc.state = PlayerComponent.State.HEAVY_ATTACK;
                else
                    pc.state = PlayerComponent.State.LIGHT_ATTACK;

                pc.attackRequested = false;
                createAttackSensor(pc);
            }

            if (pc.attacking) {
                pc.attackTimer -= deltaTime;
                if (pc.attackTimer <= 0f) {
                    removeAttackSensor(pc);
                    pc.attacking = false;
                    if (pc.jumpsLeft == pc.maxJumps) {
                        pc.state = PlayerComponent.State.IDLE;
                    } else {
                        if (pc.body.getLinearVelocity().y > 0.1f) {
                            pc.state = PlayerComponent.State.JUMP;
                        } else {
                            pc.state = PlayerComponent.State.AIRSPIN;
                        }
                    }

                    if (pc.isBlocking) {
                        pc.attackRequested = false;
                        return;
                    }
                }
            }
        }
    }

    private void createAttackSensor(PlayerComponent pc) {
        float w = 16f / CoreResources.PPM;
        float h = 30f / CoreResources.PPM;
        float offsetX = (20f / CoreResources.PPM) / 2 + w / 2;
        offsetX = pc.facingLeft ? -offsetX : offsetX;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w/2, h/2, new Vector2(offsetX, 0f), 0f);

        FixtureDef fd = new FixtureDef();
        fd.shape         = shape;
        fd.isSensor      = true;
        fd.filter.categoryBits = CollisionCategories.ATTACK;
        fd.filter.maskBits     = CollisionCategories.ENEMY_HURTBOX;

        Fixture fx = pc.body.createFixture(fd);
        fx.setUserData("playerAttack");
        pc.attackSensorFixture = fx;

        shape.dispose();
    }

    private void removeAttackSensor(PlayerComponent pc) {
        if (pc.attackSensorFixture != null) {
            pc.body.destroyFixture(pc.attackSensorFixture);
            pc.attackSensorFixture = null;
        }
    }
}
