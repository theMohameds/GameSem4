package io.group9.weapons.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import services.IWeapon;
import services.IPickable;

public class SwordComponent implements Component, IWeapon, IPickable {
    private Entity owner;

    @Override
    public String getName() {
        return "Sword";
    }

    @Override
    public void use(Entity user) {
    }
    @Override
    public void onPickUp(Entity picker) {
        this.owner = picker;
    }

    @Override
    public void onDrop(Entity picker) {
        if (picker == owner) {
            this.owner = null;
        }
    }
}
