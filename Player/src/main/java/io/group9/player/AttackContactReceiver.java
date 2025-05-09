package io.group9.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.ContactReceiver;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;

public class AttackContactReceiver implements ContactReceiver {

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
        // Check collisions:
        }if (aIsPlayerBody && bIsEnemyHurtbox || bIsPlayerAttack && aIsEnemyHurtbox) {
            PlayerComponent pc = CoreResources.getPlayerEntity().getComponent(PlayerComponent.class);
            if (pc.state == PlayerComponent.State.BLOCK) {
                Gdx.app.log("BLOCK", "Skade reduceret med 50%!");
                // Tilføj skadereduktionslogik her (f.eks. halver skaden)
            } else {
                // Normal skade
            }
        }
    }

    @Override public void endContact(Contact contact) { }

    private boolean isPlayerBody(Object bodyUserData) {
        if (bodyUserData == null) return false;

        if ("player".equals(bodyUserData)) return true;

        return bodyUserData == CoreResources.getPlayerEntity()
            .getComponent(PlayerComponent.class);
    }

    private void applyDamageToPlayer() {
        PlayerComponent pc = CoreResources.getPlayerEntity()
            .getComponent(PlayerComponent.class);
        if (pc == null || pc.state == PlayerComponent.State.DEAD) return;

        pc.health -= 10;
        CoreResources.setPlayerHealth(pc.health);

        if (pc.health <= 0) {
            pc.state = PlayerComponent.State.DEAD;
        } else {
            pc.state     = PlayerComponent.State.HURT;
            pc.hurtTimer = pc.hurtDuration;
            pc.isHurt    = true;
        }
    }
}
