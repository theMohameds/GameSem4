package io.group9.weapon.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.common.WeaponType;
import components.CollisionCategories;
import io.group9.weapon.components.WeaponComponent;

public class WeaponSystem extends EntitySystem {
    private float spawnTimer = 0f;
    private float spawnInterval = 8f;  // Spawn a new weapon every 8 seconds

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;
        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval;
            spawnWeapon();
        }

        // Loop through all weapon entities and check if they should be despawned
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
        // Randomly choose a weapon type: SWORD or KNIFE
        wc.type = MathUtils.randomBoolean() ? WeaponType.SWORD : WeaponType.KNIFE;
        wc.spawnTime = CoreResources.getCurrentTime();
        wc.isActive = true;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = 5f;
        Vector2 position = getRandomSpawnPosition();
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

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
            camera.position.x - camera.viewportWidth / 2 + padding,
            camera.position.x + camera.viewportWidth / 2 - padding
        );

        // Use a ray cast to determine the ground’s highest Y value at x
        World world = CoreResources.getWorld();
        float radius = 4f / CoreResources.PPM;
        float cameraTop = camera.position.y + camera.viewportHeight / 2;

        final float[] highestY = { -Float.MAX_VALUE };
        Vector2 rayStart = new Vector2(x, cameraTop + 500f);  // Start well above the camera view
        Vector2 rayEnd = new Vector2(x, cameraTop - camera.viewportHeight - 500f);  // Extend well below

        world.rayCast((fixture, point, normal, fraction) -> {
            if ((fixture.getFilterData().categoryBits & CollisionCategories.GROUND) != 0) {
                highestY[0] = Math.max(highestY[0], point.y);
            }
            return 1; // Continue the ray cast
        }, rayStart, rayEnd);

        if (highestY[0] == -Float.MAX_VALUE) {
            Gdx.app.error("WeaponSystem", "No ground found at x: " + x + " with camera y: " + camera.position.y);
            return new Vector2(x, cameraTop + 50f);  // Fall back to a position near the top
        }

        return new Vector2(x, highestY[0] + radius);
    }

    private void despawnWeapon(Entity weapon) {
        WeaponComponent wc = weapon.getComponent(WeaponComponent.class);
        if (wc != null && wc.body != null) {
            CoreResources.getWorld().destroyBody(wc.body);
        }
        getEngine().removeEntity(weapon);
    }
}

