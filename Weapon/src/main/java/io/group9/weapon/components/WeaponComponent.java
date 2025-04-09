package io.group9.weapon.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import io.group9.common.WeaponType;

public class WeaponComponent implements Component {
    public static WeaponType WeaponType;
    public WeaponType type;
    public boolean isActive = true;
    public float spawnTime;
    public float lifeTime = 10f;
    public Body body;
}
