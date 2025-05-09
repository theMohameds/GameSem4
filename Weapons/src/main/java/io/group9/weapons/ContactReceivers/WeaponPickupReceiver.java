package io.group9.weapons.ContactReceivers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.weapons.BodyDestroySystem;
import locators.InventoryServiceLocator;
import locators.PlayerServiceLocator;
import services.weapon.IPickable;
import services.weapon.IWeapon;
import services.weapon.IInventoryService;
import io.group9.ContactReceiver;
import services.player.IPlayerService;

public class WeaponPickupReceiver implements ContactReceiver {
    private final IPlayerService playerSvc = PlayerServiceLocator.get();
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

        if (b == playerSvc.getPlayerBody()) picker = playerSvc.getPlayerEntity();
        else if (b == CoreResources.getEnemyBody())  picker = CoreResources.getEnemyEntity();
        else return;

        IPickable pickable = (IPickable)ud;
        pickable.onPickUp(picker);

        IInventoryService inv = InventoryServiceLocator.getInventoryService();
        inv.add(picker, (IWeapon)pickable);
        Gdx.app.log("WeaponPickupReceiver", "Picked up " + " by " + picker);

        BodyDestroySystem.schedule(pickFx.getBody());
    }
}

