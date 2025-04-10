package io.group9.weapon;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.common.WeaponType;
import io.group9.weapon.components.WeaponComponent;
import io.group9.weapon.system.WeaponContactUpdateSystem;
import io.group9.weapon.system.WeaponRendererSystem;
import io.group9.weapon.system.WeaponSystem;
import plugins.ECSPlugin;
import components.CollisionCategories;

public class WeaponPlugin implements ECSPlugin {
    private WeaponContactReceiver contactReceiver;
    private static final int INITIAL_WEAPONS = 3;

    @Override
    public void registerSystems(Engine engine) {
        Gdx.app.log("WeaponPlugin", "Registering systems...");

        engine.addSystem(new WeaponSystem());
        Gdx.app.log("WeaponPlugin", "WeaponSystem added to engine");

        engine.addSystem(new WeaponRendererSystem());
        Gdx.app.log("WeaponPlugin", "WeaponRendererSystem added to engine");

        World world = CoreResources.getWorld();
        contactReceiver = new WeaponContactReceiver(engine, world);
        CoreResources.getContactDispatcher().addReceiver(contactReceiver);
        Gdx.app.log("WeaponPlugin", "WeaponContactReceiver registered");

        engine.addSystem(new WeaponContactUpdateSystem(contactReceiver));
        Gdx.app.log("WeaponPlugin", "WeaponContactUpdateSystem added");
    }

    @Override
    public void createEntities(Engine engine) {
        Gdx.app.log("WeaponPlugin", "Creating initial weapons...");
        World world = CoreResources.getWorld();
        PooledEngine pooledEngine = (PooledEngine) engine;

        for (int i = 0; i < INITIAL_WEAPONS; i++) {
            spawnWeapon(pooledEngine, world);
        }
    }

    // Spawns a weapon with two fixtures:
    // 1. A sensor fixture for player pickup (only collides with the player).
    // 2. A physical fixture for ground collision (uses default filtering so it collides like the player).
    private void spawnWeapon(PooledEngine engine, World world) {
        Entity weapon = engine.createEntity();

        WeaponComponent wc = engine.createComponent(WeaponComponent.class);
        wc.type = MathUtils.randomBoolean() ? WeaponType.SWORD : WeaponType.KNIFE;
        wc.spawnTime = CoreResources.getCurrentTime();
        wc.isActive = true;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = 5f;
        Vector2 position = getRandomSpawnPosition();
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        // --- Sensor Fixture for Pickup ---
        // This fixture is used to detect when the player picks up the weapon.
        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius(4f / CoreResources.PPM);
        FixtureDef sensorFixtureDef = new FixtureDef();
        sensorFixtureDef.shape = sensorShape;
        sensorFixtureDef.isSensor = true;
        sensorFixtureDef.filter.categoryBits = CollisionCategories.WEAPON;
        // Only register collisions with the player.
        sensorFixtureDef.filter.maskBits = CollisionCategories.PLAYER;
        body.createFixture(sensorFixtureDef);
        sensorShape.dispose();

        // --- Physical Fixture for Ground Collision ---
        // This fixture is used so that the weapon collides physically with the map.
        // We do not set filter bits here so it uses the default filtering (like the player).
        CircleShape physicalShape = new CircleShape();
        physicalShape.setRadius(4f / CoreResources.PPM);
        FixtureDef physicalFixtureDef = new FixtureDef();
        physicalFixtureDef.shape = physicalShape;
        physicalFixtureDef.density = 0.001f;
        physicalFixtureDef.restitution = 0.1f;  // Adjust bounce as needed.
        physicalFixtureDef.isSensor = false;
        body.createFixture(physicalFixtureDef);
        physicalShape.dispose();

        // Finish setting up the entity.
        body.setUserData(weapon);
        wc.body = body;
        weapon.add(wc);
        engine.addEntity(weapon);
    }

    // Spawns the weapon at a random X position within the camera view and
    // at a high Y position (e.g., 250f/PPM) so that it falls onto the map.
    private Vector2 getRandomSpawnPosition() {
        OrthographicCamera camera = CoreResources.getCamera();
        float padding = 2f;
        float x = MathUtils.random(
            camera.position.x - camera.viewportWidth / 2 + padding,
            camera.position.x + camera.viewportWidth / 2 - padding
        );
        // Spawn high at y = 250f/PPM.
        float y = 250f / CoreResources.PPM;
        return new Vector2(x, y);
    }

    @Override
    public int getPriority() {
        return 4;
    }
}



