package io.group9.player;

import com.badlogic.gdx.physics.box2d.Contact;
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
            if (CoreResources.getPlayerEntity() != null) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);
                if(!pc.isWallBound) {
                    pc.jumpsLeft = pc.maxJumps;
                    pc.state = PlayerComponent.State.IDLE;
                }
            }
        }

        // Check for wall contact
        if(("player".equals(dataA) && "wall".equals(dataB)) || ("wall".equals(dataA) && "player".equals(dataB))) {

            if (CoreResources.getPlayerEntity() != null) {
                PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);

                if(pc.jumpsLeft < pc.maxJumps && !pc.isWallBound) {
                    pc.state = PlayerComponent.State.WALL_LAND;
                    pc.isWallBound = true;
                    pc.wallBoundTimer = pc.wallBoundDuration;

                    pc.jumpsLeft = pc.maxJumps; // Reset jumps on wall contact

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
    }
}

