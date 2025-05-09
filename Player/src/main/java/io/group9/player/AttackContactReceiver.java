package io.group9.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.ContactReceiver;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;

public class AttackContactReceiver implements ContactReceiver {
    private final IPlayerService playerSvc = PlayerServiceLocator.get();

    @Override
    public void beginContact(Contact contact) {

        Object fxA = contact.getFixtureA().getUserData();
        Object fxB = contact.getFixtureB().getUserData();
        Object bdA = contact.getFixtureA().getBody().getUserData();
        Object bdB = contact.getFixtureB().getBody().getUserData();

        boolean aEnemyAtk = "enemyAttack".equals(fxA);
        boolean bEnemyAtk = "enemyAttack".equals(fxB);

        boolean aIsPlayerBody = isPlayerBody(bdA);
        boolean bIsPlayerBody = isPlayerBody(bdB);

        boolean aIsPlayerAttack   = "playerAttack".equals(fxA);
        boolean bIsPlayerAttack   = "playerAttack".equals(fxB);

        boolean aIsEnemyHurtbox  = "enemyHurtbox".equals(fxA);
        boolean bIsEnemyHurtbox  = "enemyHurtbox".equals(fxB);

        if ((aEnemyAtk && bIsPlayerBody) || (bEnemyAtk && aIsPlayerBody)) {
            applyDamageToPlayer();


        }if (aIsPlayerBody && bIsEnemyHurtbox || bIsPlayerAttack && aIsEnemyHurtbox) {
            PlayerComponent pc = playerSvc.getPlayerEntity().getComponent(PlayerComponent.class);
            if (pc.state == PlayerComponent.State.BLOCK) {
                Gdx.app.log("BLOCK", "Skade reduceret med 50%!");
            } else {
            }
        }
    }

    @Override public void endContact(Contact contact) { }

    private boolean isPlayerBody(Object bodyUserData) {
        if (bodyUserData == null) return false;

        if ("player".equals(bodyUserData)) return true;

        return bodyUserData == playerSvc.getPlayerBody();
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
