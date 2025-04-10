package io.group9.player;

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
        if (("player".equals(dataA) && "ground".equals(dataB)) ||
            ("ground".equals(dataA) && "player".equals(dataB))) {

            // Get the contact manifold
            WorldManifold worldManifold = contact.getWorldManifold();
            boolean isGroundContact = false;

            // Determine if the contact is with a surface below the player based on the normal
            if ("player".equals(dataA)) { // Player = A, Ground = B. Normal points A->B (downwards for ground contact)
                if (worldManifold.getNormal().y < -0.1f) {
                    isGroundContact = true;
                }
            } else { // Player = B, Ground = A. Normal points A->B (upwards for ground contact)
                if (worldManifold.getNormal().y > 0.1f) {
                    isGroundContact = true;
                }
            }

            if (CoreResources.getPlayerEntity() != null && isGroundContact) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);
                if(!pc.isWallBound) { // Only reset if not currently wall-bound
                    pc.jumpsLeft = pc.maxJumps;
                    pc.state = PlayerComponent.State.IDLE;
                }
            }
        }

        // Check for wall contact
        if(("player".equals(dataA) && "wall".equals(dataB)) || ("wall".equals(dataA) && "player".equals(dataB))) {

            if (CoreResources.getPlayerEntity() != null) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);

                if(!pc.isWallBound) {
                    pc.state = PlayerComponent.State.WALL_LAND;
                    pc.isWallBound = true;
                    pc.wallBoundTimer = pc.wallBoundDuration;

                    //Stop vertical movement
                    if(pc.body != null){
                        pc.body.setLinearVelocity(pc.body.getLinearVelocity().x, 0);
                        pc.body.setGravityScale(0);
                    }
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Optional: handle end contact if needed.
        Object dataA = contact.getFixtureA().getBody().getUserData();
        Object dataB = contact.getFixtureB().getBody().getUserData();

        // Check if player is leaving a wall
        if(("player".equals(dataA) && "wall".equals(dataB)) || ("wall".equals(dataA) && "player".equals(dataB))) {
            if (CoreResources.getPlayerEntity() != null) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);
                if (pc.isWallBound) {
                    pc.isWallBound = false;
                    pc.wallBoundTimer = 0; // Reset timer
                    if(pc.body != null) {
                        pc.body.setGravityScale(1); // Restore gravity
                    }
                    // Set state to JUMP (general airborne state) after leaving wall
                    pc.state = PlayerComponent.State.JUMP;
                }
            }
        }
    }
}

