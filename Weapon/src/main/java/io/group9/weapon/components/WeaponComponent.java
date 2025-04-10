package io.group9.weapon.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import io.group9.common.WeaponType;

public class WeaponComponent implements Component {
    public WeaponType type;        // e.g., WeaponType.SWORD or WeaponType.KNIFE
    public boolean isActive = true;
    public float spawnTime;          // Time when the weapon was spawned
    public float lifeTime = 30f;     // How long the weapon remains active before despawning
    public Body body;                // The Box2D body for physics
}

