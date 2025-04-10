package io.group9.weapon.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.common.WeaponType;
import components.CollisionCategories;
import com.badlogic.ashley.core.Family;
import io.group9.weapon.components.WeaponComponent;

public class WeaponSystem extends EntitySystem {
    private float spawnTimer = 0f;
    private float spawnInterval = 8f;

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;
        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval;
            spawnWeapon();
        }

        for (Entity entity : getEngine().getEntitiesFor(Family.all(WeaponComponent.class).get())) {
            WeaponComponent wc = entity.getComponent(WeaponComponent.class);
            if (wc.isActive && (CoreResources.getCurrentTime() - wc.spawnTime) >= wc.lifeTime) {
                despawnWeapon(entity);
            }
        }
    }

    private void spawnWeapon() {
        PooledEngine engine = (PooledEngine) getEngine();
        Entity weapon = engine.createEntity();

        WeaponComponent wc = engine.createComponent(WeaponComponent.class);
        wc.type = MathUtils.randomBoolean() ? WeaponType.SWORD : WeaponType.KNIFE;
        wc.spawnTime = CoreResources.getCurrentTime();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = 5f;
        Vector2 position = getRandomSpawnPosition();
        bodyDef.position.set(position);

        Body body = CoreResources.getWorld().createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(4f / CoreResources.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.001f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = CollisionCategories.WEAPON;
        fixtureDef.filter.maskBits = (short) (CollisionCategories.PLAYER | CollisionCategories.GROUND);

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData(weapon);
        wc.body = body;

        weapon.add(wc);
        engine.addEntity(weapon);
    }

    private Vector2 getRandomSpawnPosition() {
        OrthographicCamera camera = CoreResources.getCamera();
        float padding = 2f;
        float x = MathUtils.random(
            camera.position.x - camera.viewportWidth/2 + padding,
            camera.position.x + camera.viewportWidth/2 - padding
        );
        float y = camera.viewportHeight/2 - padding;
        return new Vector2(x, y);
    }

    private void despawnWeapon(Entity weapon) {
        WeaponComponent wc = weapon.getComponent(WeaponComponent.class);
        if (wc != null && wc.body != null) {
            CoreResources.getWorld().destroyBody(wc.body);
        }
        getEngine().removeEntity(weapon);
    }
}
