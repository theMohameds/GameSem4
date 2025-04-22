package io.group9.enemy.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import io.group9.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;
import components.CollisionCategories;
import com.badlogic.gdx.physics.box2d.Fixture;

public class EnemyAttackSystem extends EntitySystem {
    private ImmutableArray<Entity> ents;

    @Override
    public void addedToEngine(Engine engine) {
        ents = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        for (Entity e : ents) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);

            // Remove old attack sensor if pending
            if (ec.pendingRemoveSensor && ec.attackSensor != null) {
                ec.body.destroyFixture(ec.attackSensor);
                ec.attackSensor = null;
                ec.pendingRemoveSensor = false;
            }

            // Start a new attack if requested
            if (ec.attackRequested && !ec.attacking) {
                ec.attacking = true;
                ec.attackTimer = ec.attackDuration;
                ec.attackRequested = false;

                // Set state to ATTACK
                ec.state = EnemyState.ATTACK;
                ec.animTime = 0f;

                spawnSensor(ec);
            }

            // Update ongoing attack
            if (ec.attacking) {
                ec.attackTimer -= dt;
                if (ec.attackTimer <= 0f) {
                    ec.attacking = false;
                    ec.pendingRemoveSensor = true;
                }
            }
        }
    }

    private void spawnSensor(EnemyComponent ec) {
        float w = ec.sensorW / CoreResources.PPM;
        float h = ec.sensorH / CoreResources.PPM;
        float offsetX = ec.facingLeft
            ? -((20f / CoreResources.PPM) / 2 + w / 2)
            :  ((20f / CoreResources.PPM) / 2 + w / 2);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w / 2, h / 2, new Vector2(offsetX, 0f), 0f);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = true;
        fd.filter.categoryBits = CollisionCategories.ATTACK;
        fd.filter.maskBits = CollisionCategories.PLAYER;

        Fixture sensor = ec.body.createFixture(fd);
        sensor.setUserData("enemyAttack");
        ec.attackSensor = sensor;

        shape.dispose();
    }
}
