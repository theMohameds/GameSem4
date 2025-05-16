package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.*;
import data.WorldProvider;
import util.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import components.CollisionCategories;

public class EnemyAttackSystem extends EntitySystem {
    private ImmutableArray<Entity> enemies;

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        World world = WorldProvider.getWorld();


        for (Entity e : enemies) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);

            if (ec.attackRequested && !ec.attacking) {
                ec.attacking       = true;
                ec.attackTimer     = ec.attackDuration;
                ec.attackRequested = false;

                PolygonShape shape = new PolygonShape();
                float w = ec.sensorW / CoreResources.PPM;
                float h = ec.sensorH / CoreResources.PPM;
                float offsetX = ec.facingLeft
                    ? -((ec.boundingRadius) + w/2)
                    :  (ec.boundingRadius + w/2);
                shape.setAsBox(w/2, h/2, new com.badlogic.gdx.math.Vector2(offsetX, 0f), 0f);

                FixtureDef fd = new FixtureDef();
                fd.shape         = shape;
                fd.isSensor      = true;
                fd.filter.categoryBits = CollisionCategories.ENEMY_ATTACK;
                fd.filter.maskBits     = CollisionCategories.PLAYER;
                ec.attackSensor = ec.body.createFixture(fd);
                ec.attackSensor.setUserData("enemyAttack");

                shape.dispose();
            }

            if (ec.attacking) {
                ec.attackTimer -= deltaTime;
                if (ec.attackTimer <= 0f) {
                    if (ec.attackSensor != null) {
                        ec.body.destroyFixture(ec.attackSensor);
                        ec.attackSensor = null;
                    }
                    ec.attacking = false;
                }
            }
        }
    }
}
