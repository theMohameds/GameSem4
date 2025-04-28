package services;

import com.badlogic.ashley.core.Entity;

public interface IWeapon {
    String getName();

    void use(Entity user);

    void onDrop(Entity owner);
}
