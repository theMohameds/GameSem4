package io.group9.player;

import com.badlogic.gdx.Gdx;
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

        WorldManifold manifold = contact.getWorldManifold();
        Vector2 normal = manifold.getNormal();

        boolean isAPlayer = "player".equals(dataA);
        boolean isBPlayer = "player".equals(dataB);
        if (!isAPlayer && !isBPlayer) {
            return;
        }

        Object otherData = isAPlayer ? dataB : dataA;

        if ("ground".equals(otherData) || "wall".equals(otherData)) {
            if (CoreResources.getPlayerEntity() != null) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);

                if (normal.y > 0.5f) {  //
                    pc.jumpsLeft = pc.maxJumps;
                    pc.state = PlayerComponent.State.IDLE;
                    pc.wallHanging = false;
                    Gdx.app.log("PlayerContactReceiver", "Ground contact detected.");
                }
                else if (Math.abs(normal.x) > Math.abs(normal.y)) {
                    if (normal.x > 0) {
                        pc.facingLeft = true;
                    } else if (normal.x < 0) {
                        pc.facingLeft = false;
                    }

                    // 2) Trigger wall-hang logic
                    pc.wallHanging = true;
                    pc.wallHangingTimer = 0f; // reset the timer if you use one
                    pc.state = PlayerComponent.State.LAND_WALL;
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Optional: handle end contact logic if needed.
    }

    // Example helper method; adjust based on how you manage player input.
    private boolean playerIsPressingTowardsWall() {
        // Implement input checking here. For example:
        // return Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        // Alternatively, compare the player's facing direction with the side of the collision.
        return true;  // Placeholder, replace with actual directional input logic.
    }
}
