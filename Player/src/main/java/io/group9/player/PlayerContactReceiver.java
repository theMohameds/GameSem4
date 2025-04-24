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

        Object bdA = contact.getFixtureA().getBody().getUserData();
        Object bdB = contact.getFixtureB().getBody().getUserData();

        boolean isAPlayer = isPlayerBody(bdA);
        boolean isBPlayer = isPlayerBody(bdB);
        if (!isAPlayer && !isBPlayer) return;

        Object otherData = isAPlayer ? bdB : bdA;


        if (!"ground".equals(otherData) && !"wall".equals(otherData)) return;

        PlayerComponent pc = CoreResources.getPlayerEntity()
            .getComponent(PlayerComponent.class);
        if (pc == null) return;

        WorldManifold manifold = contact.getWorldManifold();
        Vector2 normal = manifold.getNormal();

        if (normal.y > 0.5f) {
            pc.jumpsLeft   = pc.maxJumps;
            pc.wallHanging = false;
            pc.state       = PlayerComponent.State.IDLE;
        } else if (Math.abs(normal.x) > Math.abs(normal.y)) {
            // LAND on wall
            if (pc.jumpsLeft == pc.maxJumps) return;
            pc.wallOnLeft  = normal.x > 0;
            pc.facingLeft  = pc.wallOnLeft;
            pc.wallHanging = true;
            pc.wallHangingTimer = 0f;
            pc.state       = PlayerComponent.State.LAND_WALL;
        }
    }

    @Override public void endContact(Contact contact) {}


    private boolean isPlayerBody(Object userData) {
        if (userData == null) return false;
        if ("player".equals(userData)) return true;
        PlayerComponent pc = CoreResources.getPlayerEntity()
            .getComponent(PlayerComponent.class);
        return userData == pc;
    }
}
