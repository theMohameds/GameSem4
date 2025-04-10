package io.group9.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import io.group9.ContactReceiver;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;

public class PlayerContactReceiver implements ContactReceiver {
    @Override
    public void beginContact(Contact contact) {
        Object dataA = contact.getFixtureA().getBody().getUserData();
        Object dataB = contact.getFixtureB().getBody().getUserData();

        // Retrieve the world manifold to get the normal vector of the contact.
        WorldManifold manifold = contact.getWorldManifold();
        Vector2 normal = manifold.getNormal();

        // Only reset jumpcount on true ground contacts:
        // Check that the collision is more vertical than horizontal.
        if (("player".equals(dataA) && "ground".equals(dataB)) ||
            ("ground".equals(dataA) && "player".equals(dataB))) {

            if (normal.y > 0.5f) {  // Adjust threshold as needed.
                if (CoreResources.getPlayerEntity() != null) {
                    PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);
                    pc.jumpsLeft = pc.maxJumps;
                    pc.state = PlayerComponent.State.IDLE;
                    pc.wallHanging = false; // Reset wall hanging flag on true ground landing.
                }
            }
        }

        // Wall contact: Only process collisions that are mainly horizontal.
        if (("player".equals(dataA) && "wall".equals(dataB)) ||
            ("wall".equals(dataA) && "player".equals(dataB))) {

            if (CoreResources.getPlayerEntity() != null) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);
                if (Math.abs(normal.x) > Math.abs(normal.y)) {
                    // Only if the player is falling do we set wall-hanging.
                    if (pc.state == PlayerComponent.State.JUMP) {
                        pc.state = PlayerComponent.State.LAND_WALL;
                        pc.wallHanging = true;
                    }
                }
            }
    }
        }

    @Override
    public void endContact(Contact contact) {
        // Optional: handle end contact if needed.
    }

}

