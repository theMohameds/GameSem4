package io.group9.weapons.plugins;

import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.weapons.components.SwordComponent;

public class WeaponFactory {
    public static void spawnSword(float xPx, float yPx) {
        World world = CoreResources.getWorld();
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(xPx / CoreResources.PPM, yPx / CoreResources.PPM);
        Body body = world.createBody(bd);

        // Main body
        CircleShape dropShape = new CircleShape();
        dropShape.setRadius(6f / CoreResources.PPM);
        FixtureDef dropFd = new FixtureDef();
        dropFd.shape = dropShape;
        dropFd.density = 0.5f;
        body.createFixture(dropFd);
        dropShape.dispose();

        // Sensor for pickup
        SwordComponent weapon = new SwordComponent();
        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius(10f / CoreResources.PPM);
        FixtureDef sensorFd = new FixtureDef();
        sensorFd.shape = sensorShape;
        sensorFd.isSensor = true;
        Fixture sensorFx = body.createFixture(sensorFd);

        sensorFx.setUserData(weapon);
        sensorShape.dispose();
    }

}
