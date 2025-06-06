package io.group9.weapons.plugins;

import com.badlogic.gdx.physics.box2d.*;
import data.WorldProvider;
import data.util.CoreResources;
import io.group9.weapons.components.SwordComponent;
import services.weapon.IWeapon;

public class WeaponFactory {
    public static void spawnWeapon(IWeapon weapon, float xPx, float yPx) {
        World world = WorldProvider.getWorld();

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(xPx / CoreResources.PPM, yPx / CoreResources.PPM);
        Body body = world.createBody(bd);

        CircleShape dropShape = new CircleShape();
        dropShape.setRadius(6f / CoreResources.PPM);
        FixtureDef dropFd = new FixtureDef();
        dropFd.shape = dropShape;
        dropFd.density = 0.5f;
        body.createFixture(dropFd);
        dropShape.dispose();

        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius(10f / CoreResources.PPM);
        FixtureDef sensorFd = new FixtureDef();
        sensorFd.shape = sensorShape;
        sensorFd.isSensor = true;
        Fixture sensorFx = body.createFixture(sensorFd);
        sensorShape.dispose();

        sensorFx.setUserData(weapon);
    }

    public static void spawnSword(float xPx, float yPx) {
        spawnWeapon(new SwordComponent(), xPx, yPx);
    }

}
