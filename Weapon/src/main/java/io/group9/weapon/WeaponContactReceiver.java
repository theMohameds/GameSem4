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

        // Check if one is PLAYER and the other is WEAPON
        if ((categoryA == CollisionCategories.PLAYER && categoryB == CollisionCategories.WEAPON)) {
            playerEntity = (Entity) fixtureA.getBody().getUserData();
            weaponEntity = (Entity) fixtureB.getBody().getUserData();
        } else if ((categoryB == CollisionCategories.PLAYER && categoryA == CollisionCategories.WEAPON)) {
            playerEntity = (Entity) fixtureB.getBody().getUserData();
            weaponEntity = (Entity) fixtureA.getBody().getUserData();
        }

        if (playerEntity != null && weaponEntity != null) {
            WeaponComponent wc = weaponEntity.getComponent(WeaponComponent.class);
            if (wc != null && wc.isActive) {
                // Handle weapon pickup here (e.g., trigger an event or modify another component)
                engine.removeEntity(weaponEntity);
                CoreResources.getWorld().destroyBody(wc.body);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {}

    public void update() {
    }
}
