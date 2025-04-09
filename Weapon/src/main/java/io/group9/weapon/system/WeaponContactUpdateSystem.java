package io.group9.weapon.system;

import com.badlogic.ashley.core.EntitySystem;
import io.group9.weapon.WeaponContactReceiver;

public class WeaponContactUpdateSystem extends EntitySystem {
    private final WeaponContactReceiver contactReceiver;

    public WeaponContactUpdateSystem(WeaponContactReceiver contactReceiver) {
        this.contactReceiver = contactReceiver;
    }

    @Override
    public void update(float deltaTime) {
        contactReceiver.update();
    }
}
