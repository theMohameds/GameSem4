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

        // Only proceed if one of the bodies is the player.
        boolean isAPlayer = "player".equals(dataA);
        boolean isBPlayer = "player".equals(dataB);
        if (!isAPlayer && !isBPlayer) {
            return;
        }
        Object otherData = isAPlayer ? dataB : dataA;

        // Process contact only if colliding with ground or wall.
        if ("ground".equals(otherData) || "wall".equals(otherData)) {
            WorldManifold manifold = contact.getWorldManifold();
            Vector2 normal = manifold.getNormal();

            // Use the player component from the global resources.
            if (CoreResources.getPlayerEntity() == null) {
                return;
            }
            PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);

            if (normal.y > 0.5f) {
                processGroundContact(pc);
            } else if (Math.abs(normal.x) > Math.abs(normal.y)) {
                processWallContact(pc, normal);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {}

    private void processGroundContact(PlayerComponent pc) {
        pc.jumpsLeft = pc.maxJumps;
        pc.wallHanging = false;
        pc.state = PlayerComponent.State.IDLE;
        Gdx.app.log("PlayerContactReceiver", "Ground contact detected. Reset jumps and state to IDLE.");
    }

    private void processWallContact(PlayerComponent pc, Vector2 normal) {
        if(pc.jumpsLeft == pc.maxJumps) return ;
        if (normal.x > 0) {
            // Wall is on the left; player should face left.
            pc.wallOnLeft = true;
            pc.facingLeft = true;
        } else {
            // Wall is on the right; player should face right.
            pc.wallOnLeft = false;
            pc.facingLeft = false;
        }
        pc.wallHanging = true;
        pc.wallHangingTimer = 0f; // Reset hanging timer.
        pc.state = PlayerComponent.State.LAND_WALL;
        Gdx.app.log("PlayerContactReceiver", "Wall contact detected. Engaging wall hang.");
    }
}
