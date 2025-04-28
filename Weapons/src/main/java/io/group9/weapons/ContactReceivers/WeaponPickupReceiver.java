package io.group9.weapons.ContactReceivers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.weapons.BodyDestroySystem;
import services.IPickable;
import services.IWeapon;
import services.IInventoryService;
import io.group9.ContactReceiver;

public class WeaponPickupReceiver implements ContactReceiver {
    @Override public void beginContact(Contact c) {
        tryOne(c.getFixtureA(), c.getFixtureB());
        tryOne(c.getFixtureB(), c.getFixtureA());
    }
    @Override public void endContact(Contact c){}

    private void tryOne(Fixture pickFx, Fixture actorFx) {
        Object ud = pickFx.getUserData();
        if (!(ud instanceof IPickable)) return;

        Body b = actorFx.getBody();
        Entity picker;
        if      (b == CoreResources.getPlayerBody()) picker = CoreResources.getPlayerEntity();
        else if (b == CoreResources.getEnemyBody())  picker = CoreResources.getEnemyEntity();
        else return;

        IPickable pickable = (IPickable)ud;
        pickable.onPickUp(picker);

        IInventoryService inv = CoreResources.getInventoryService();
        inv.add(picker, (IWeapon)pickable);

        BodyDestroySystem.schedule(pickFx.getBody());
    }
}

