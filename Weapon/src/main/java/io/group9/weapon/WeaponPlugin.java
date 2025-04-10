package io.group9.weapon;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import components.CollisionCategories;
import io.group9.CoreResources;
import io.group9.common.WeaponType;
import io.group9.weapon.components.WeaponComponent;
import io.group9.weapon.system.WeaponContactUpdateSystem;
import plugins.ECSPlugin;
import io.group9.weapon.system.WeaponRendererSystem;
import io.group9.weapon.system.WeaponSystem;

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

        for(int i = 0; i < INITIAL_WEAPONS; i++) {
            spawnWeapon(pooledEngine, world);
        }
    }

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

        World world = CoreResources.getWorld();
        float radius = 4f / CoreResources.PPM;
        float cameraTop = camera.position.y + camera.viewportHeight/2;

        final float[] highestY = {-Float.MAX_VALUE};
        Vector2 rayStart = new Vector2(x, cameraTop + 500f);  // Start 500 units above camera view
        Vector2 rayEnd = new Vector2(x, cameraTop - camera.viewportHeight - 500f);  // Extend below view

        world.rayCast((fixture, point, normal, fraction) -> {
            if ((fixture.getFilterData().categoryBits & CollisionCategories.GROUND) != 0) {
                highestY[0] = Math.max(highestY[0], point.y);
            }
            return 1; // Continue searching
        }, rayStart, rayEnd);

        if (highestY[0] == -Float.MAX_VALUE) {
            Gdx.app.error("WeaponPlugin", "No ground found at X: " + x + " Camera Y: " + camera.position.y);
            return new Vector2(x, cameraTop + 50f);  // Fallback to camera top
        }

        return new Vector2(x, highestY[0] + radius);
    }


    @Override
    public int getPriority() {
        return 3;
    }
}
