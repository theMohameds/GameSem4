package io.group9.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import contact.ContactReceiver;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;

public class PlayerContactReceiver implements ContactReceiver {
    private final IPlayerService playerSvc = PlayerServiceLocator.get();

    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        boolean aIsPlayer = bodyA == playerSvc.getPlayerBody();
        boolean bIsPlayer = bodyB == playerSvc.getPlayerBody();
        if (!aIsPlayer && !bIsPlayer) return;

        Fixture otherFx = aIsPlayer
            ? contact.getFixtureB()
            : contact.getFixtureA();

        Object otherData = otherFx.getUserData();

        if (otherData == null) {
            otherData = otherFx.getBody().getUserData();
        }

        if (!"ground".equals(otherData) && !"wall".equals(otherData)) return;

        PlayerComponent pc = (PlayerComponent) playerSvc.getPlayerBody().getUserData();
        if (pc == null) return;

        WorldManifold manifold = contact.getWorldManifold();
        Vector2 normal = manifold.getNormal();

        if (normal.y > 0.5f) {
            pc.jumpsLeft   = pc.maxJumps;
            pc.wallHanging = false;
            pc.state       = PlayerComponent.State.IDLE;
        } else if (Math.abs(normal.x) > Math.abs(normal.y)) {
            if (pc.jumpsLeft == pc.maxJumps) return;
            pc.wallOnLeft       = normal.x > 0;
            pc.facingLeft       = pc.wallOnLeft;
            pc.wallHanging      = true;
            pc.wallHangingTimer = 0f;
            pc.state            = PlayerComponent.State.LAND_WALL;
        }
    }

    @Override public void endContact(Contact contact) {}
}
