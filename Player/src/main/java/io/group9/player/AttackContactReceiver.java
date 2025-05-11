package io.group9.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Body;

import io.group9.ContactReceiver;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;

public class AttackContactReceiver implements ContactReceiver {
    private final IPlayerService playerSvc = PlayerServiceLocator.get();

    @Override
    public void beginContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        Object udA = fA.getUserData();
        Object udB = fB.getUserData();

        Body bodyA = fA.getBody();
        Body bodyB = fB.getBody();
        Body playerBody = playerSvc.getPlayerBody();

        if (("enemyAttack".equals(udA) && bodyB == playerBody) ||
            ("enemyAttack".equals(udB) && bodyA == playerBody)) {
            applyDamageToPlayer();
        }

    }

    @Override
    public void endContact(Contact contact) {
    }

    private void applyDamageToPlayer() {
        Entity playerEnt = playerSvc.getPlayerEntity();
        PlayerComponent pc = playerEnt.getComponent(PlayerComponent.class);
        if (pc == null || pc.state == PlayerComponent.State.DEAD) return;

        int newHealth = pc.health - 10;
        pc.health = newHealth;
        playerSvc.setHealth(newHealth);

        if (newHealth <= 0) {
            pc.state = PlayerComponent.State.DEAD;
        } else {
            pc.state     = PlayerComponent.State.HURT;
            pc.hurtTimer = pc.hurtDuration;
            pc.isHurt    = true;
        }
    }
}

