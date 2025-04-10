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
    // 1. A sensor fixture for player pickup (colliding only with the player).
    // 2. A physical fixture for ground collision (so it will land).
    private void spawnWeapon(PooledEngine engine, World world) {
        Entity weapon = engine.createEntity();

        WeaponComponent wc = engine.createComponent(WeaponComponent.class);
        wc.type = MathUtils.randomBoolean() ? WeaponType.SWORD : WeaponType.KNIFE;
        wc.spawnTime = CoreResources.getCurrentTime();
        wc.isActive = true;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = 5f;
        // Use a fixed high Y so the weapon gets time to fall.
        Vector2 position = getRandomSpawnPosition();
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        // --- Sensor Fixture for Pickup ---
        // Detects collision with the player.
        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius(4f / CoreResources.PPM);
        FixtureDef sensorFixtureDef = new FixtureDef();
        sensorFixtureDef.shape = sensorShape;
        sensorFixtureDef.isSensor = true;
        sensorFixtureDef.filter.categoryBits = CollisionCategories.WEAPON;
        sensorFixtureDef.filter.maskBits = CollisionCategories.PLAYER;
        body.createFixture(sensorFixtureDef);
        sensorShape.dispose();

        // --- Physical Fixture for Ground Collision ---
        // This fixture allows the weapon to interact with the ground.
        CircleShape physicalShape = new CircleShape();
        physicalShape.setRadius(4f / CoreResources.PPM);
        FixtureDef physicalFixtureDef = new FixtureDef();
        physicalFixtureDef.shape = physicalShape;
        physicalFixtureDef.density = 0.001f;
        physicalFixtureDef.restitution = 0.1f; // Adjust bounce as needed.
        physicalFixtureDef.isSensor = false;
        // Using default collision filtering so it collides with the ground as expected.
        body.createFixture(physicalFixtureDef);
        physicalShape.dispose();

        // Finalize the weapon entity.
        body.setUserData(weapon);
        wc.body = body;
        weapon.add(wc);
        engine.addEntity(weapon);
    }

    // Returns a random X (within the camera view) and a fixed high Y.
    // This function does not raycast for ground so that weapons have time to fall.
    private Vector2 getRandomSpawnPosition() {
        OrthographicCamera camera = CoreResources.getCamera();
        float padding = 2f;
        float x = MathUtils.random(
            camera.position.x - camera.viewportWidth / 2 + padding,
            camera.position.x + camera.viewportWidth / 2 - padding
        );
        // Spawn high. Adjust 250f/PPM as needed to ensure weapons have room to fall.
        float y = 250f / CoreResources.PPM;
        return new Vector2(x, y);
    }

    @Override
    public int getPriority() {
        return 4;
    }
}



