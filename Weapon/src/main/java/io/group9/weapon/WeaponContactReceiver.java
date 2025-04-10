package io.group9.weapon;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import components.CollisionCategories;
import io.group9.ContactReceiver;
import io.group9.CoreResources;
import io.group9.weapon.components.WeaponComponent;

public class WeaponContactReceiver implements ContactReceiver {
    private Engine engine;

    public WeaponContactReceiver(Engine engine, World world) {
        this.engine = engine;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        short categoryA = fixtureA.getFilterData().categoryBits;
        short categoryB = fixtureB.getFilterData().categoryBits;

        Entity playerEntity = null;
        Entity weaponEntity = null;

        // Check if one fixture belongs to the player and the other to the weapon.
        if (categoryA == CollisionCategories.PLAYER && categoryB == CollisionCategories.WEAPON) {
            Object playerData = fixtureA.getBody().getUserData();
            Object weaponData = fixtureB.getBody().getUserData();
            if (playerData instanceof Entity) {
                playerEntity = (Entity) playerData;
            }
            if (weaponData instanceof Entity) {
                weaponEntity = (Entity) weaponData;
            }
        } else if (categoryB == CollisionCategories.PLAYER && categoryA == CollisionCategories.WEAPON) {
            Object playerData = fixtureB.getBody().getUserData();
            Object weaponData = fixtureA.getBody().getUserData();
            if (playerData instanceof Entity) {
                playerEntity = (Entity) playerData;
            }
            if (weaponData instanceof Entity) {
                weaponEntity = (Entity) weaponData;
            }
        }

        if (playerEntity != null && weaponEntity != null) {
            WeaponComponent wc = weaponEntity.getComponent(WeaponComponent.class);
            if (wc != null && wc.isActive) {
                // Handle the weapon pickup.
                engine.removeEntity(weaponEntity);
                CoreResources.getWorld().destroyBody(wc.body);
            }
        }
    }

    @Override
    public void endContact(Contact contact) { }

    public void update() {
        // Additional per-frame logic for weapon collisions if needed.
    }
}


