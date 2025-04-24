package io.group9.weapon;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import java.util.ArrayList;
import java.util.List;
import components.CollisionCategories;
import io.group9.ContactReceiver;
import io.group9.CoreResources;
import io.group9.weapon.components.WeaponComponent;

public class WeaponContactReceiver implements ContactReceiver {
    private Engine engine;
    // List to store weapon entities scheduled for removal after the physics step.
    private List<Entity> weaponsToRemove;

    public WeaponContactReceiver(Engine engine, World world) {
        this.engine = engine;
        this.weaponsToRemove = new ArrayList<>();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        short categoryA = fixtureA.getFilterData().categoryBits;
        short categoryB = fixtureB.getFilterData().categoryBits;

        // Ignore weapon-to-weapon collisions.
        if (categoryA == CollisionCategories.WEAPON && categoryB == CollisionCategories.WEAPON) {
            return;
        }

        // Check for player–weapon collisions.
        // Note: The player body userData is set to "player" (a String) in your PlayerPlugin.
        if (categoryA == CollisionCategories.PLAYER && categoryB == CollisionCategories.WEAPON) {
            Object playerData = fixtureA.getBody().getUserData();
            Object weaponData = fixtureB.getBody().getUserData();
            if ("player".equals(playerData) && (weaponData instanceof Entity)) {
                Entity weaponEntity = (Entity) weaponData;
                if (!weaponsToRemove.contains(weaponEntity)) {
                    weaponsToRemove.add(weaponEntity);
                }
            }
        } else if (categoryB == CollisionCategories.PLAYER && categoryA == CollisionCategories.WEAPON) {
            Object playerData = fixtureB.getBody().getUserData();
            Object weaponData = fixtureA.getBody().getUserData();
            if ("player".equals(playerData) && (weaponData instanceof Entity)) {
                Entity weaponEntity = (Entity) weaponData;
                if (!weaponsToRemove.contains(weaponEntity)) {
                    weaponsToRemove.add(weaponEntity);
                }
            }
        }
        // Any collision that is not exactly player–weapon is ignored.
    }

    @Override
    public void endContact(Contact contact) {
        // No logic on endContact is needed.
    }

    // This update() method should be called from a system (after the physics step completes)
    // so that body removal happens while the world is unlocked.
    public void update() {
        for (Entity weaponEntity : weaponsToRemove) {
            WeaponComponent wc = weaponEntity.getComponent(WeaponComponent.class);
            if (wc != null && wc.isActive && wc.body != null) {
                engine.removeEntity(weaponEntity);
                CoreResources.getWorld().destroyBody(wc.body);
            }
        }
        weaponsToRemove.clear();
    }
}





