package io.group9.weapon.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.group9.CoreResources;
import io.group9.weapon.components.WeaponComponent;
import components.CollisionCategories;

public class WeaponSystem extends EntitySystem {
    // Minimum time (in seconds) for a weapon to be allowed to fall.
    private static final float MIN_FALL_TIME = 7f;
    // Raycast distance in world units (adjust if needed)
    private static final float RAYCAST_DISTANCE = 10f / CoreResources.PPM;
    // Threshold for vertical velocity to consider the weapon has landed.
    private static final float LANDING_VELOCITY_THRESHOLD = 0.1f;

    @Override
    public void update(float deltaTime) {
        // Loop through all weapon entities.
        for (Entity entity : getEngine().getEntitiesFor(Family.all(WeaponComponent.class).get())) {
            WeaponComponent wc = entity.getComponent(WeaponComponent.class);
            float age = CoreResources.getCurrentTime() - wc.spawnTime;

            // Only check for ground after the weapon has fallen for at least MIN_FALL_TIME seconds.
            if (age < MIN_FALL_TIME) {
                continue;
            }

            // If the weapon's vertical speed is low, assume it's landed.
            float vy = wc.body.getLinearVelocity().y;
            if (Math.abs(vy) < LANDING_VELOCITY_THRESHOLD) {
                // Weapon is at rest (landed); do not remove it.
                continue;
            }

            // Otherwise, perform a raycast from just below the weapon downwards to detect ground.
            final boolean[] groundFound = { false };
            Vector2 weaponPos = wc.body.getPosition();
            // Start a little below the center (assuming the weapon's radius is 4/PPM)
            Vector2 rayStart = new Vector2(weaponPos.x, weaponPos.y - (4f / CoreResources.PPM));
            Vector2 rayEnd = new Vector2(weaponPos.x, weaponPos.y - RAYCAST_DISTANCE);

            World world = CoreResources.getWorld();
            world.rayCast((fixture, point, normal, fraction) -> {
                if ((fixture.getFilterData().categoryBits & CollisionCategories.GROUND) != 0) {
                    groundFound[0] = true;
                    // Terminate the raycast as soon as ground is detected.
                    return 0f;
                }
                return 1f;
            }, rayStart, rayEnd);

            // If no ground is detected below a falling weapon, despawn it.
            if (!groundFound[0]) {
                Gdx.app.log("WeaponSystem", "No ground found for weapon at x: " + weaponPos.x);
                despawnWeapon(entity);
            }
        }
    }

    private void despawnWeapon(Entity weapon) {
        WeaponComponent wc = weapon.getComponent(WeaponComponent.class);
        if (wc != null && wc.body != null) {
            CoreResources.getWorld().destroyBody(wc.body);
        }
        getEngine().removeEntity(weapon);
    }
}


