package io.group9.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.systems.EnemyAnimationRenderer;
import io.group9.enemy.systems.EnemySystem;
import components.CollisionCategories;
import plugins.ECSPlugin;

public class EnemyPlugin implements ECSPlugin {
    @Override
    public void registerSystems(Engine engine) {
        engine.addSystem(new EnemySystem());
        engine.addSystem(new EnemyAnimationRenderer());
    }

    @Override
    public void createEntities(Engine engine) {
        Gdx.app.log("EnemyPlugin", "Creating Enemy with body and hurtbox sensor");

        World world = CoreResources.getWorld();
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(200f / CoreResources.PPM, 150f / CoreResources.PPM);
        bd.fixedRotation = true;
        Body body = world.createBody(bd);
        body.setSleepingAllowed(false);

        // Physical fixture (collides with ground/walls)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / CoreResources.PPM, 15 / CoreResources.PPM);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.5f;
        fd.friction = 0.2f;
        fd.restitution = 0f;
        fd.filter.categoryBits = CollisionCategories.ENEMY;
        fd.filter.maskBits = (short) 0xFFFF;
        body.createFixture(fd).setUserData("enemyBody");
        shape.dispose();

        // Sensor fixture (detects attack hits)
        PolygonShape hurtShape = new PolygonShape();
        hurtShape.setAsBox(8 / CoreResources.PPM, 15 / CoreResources.PPM);
        FixtureDef hurtFD = new FixtureDef();
        hurtFD.shape = hurtShape;
        hurtFD.isSensor = true;
        hurtFD.filter.categoryBits = CollisionCategories.ENEMY_HURTBOX;
        hurtFD.filter.maskBits = CollisionCategories.ATTACK;
        body.createFixture(hurtFD).setUserData("enemyHurtbox");
        hurtShape.dispose();

        // Create Enemy entity
        Entity enemy = new Entity();
        EnemyComponent enemyComponent = new EnemyComponent();
        enemyComponent.body = body;
        body.setUserData(enemyComponent); // Enable Damageable interface use

        enemy.add(enemyComponent);
        engine.addEntity(enemy);
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
