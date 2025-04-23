package io.group9.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.ContactReceiver;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;

public class AttackContactReceiver implements ContactReceiver {
    @Override
    public void beginContact(Contact contact) {
        // Fixture userData
        Object fA = contact.getFixtureA().getUserData();
        Object fB = contact.getFixtureB().getUserData();
        // Body userData
        Object bA = contact.getFixtureA().getBody().getUserData();
        Object bB = contact.getFixtureB().getBody().getUserData();

        // Did an enemyAttack sensor hit the player body?
        boolean aIsEnemyAtk   = "enemyAttack".equals(fA);
        boolean bIsEnemyAtk   = "enemyAttack".equals(fB);
        boolean aIsPlayerBody = "player".equals(bA);
        boolean bIsPlayerBody = "player".equals(bB);

        if ((aIsEnemyAtk && bIsPlayerBody) || (bIsEnemyAtk && aIsPlayerBody)) {
            hitPlayer();
        }
    }

    @Override
    public void endContact(Contact contact) { }

    private void hitPlayer() {
        if (CoreResources.getPlayerEntity() == null) return;
        PlayerComponent pc = CoreResources
            .getPlayerEntity()
            .getComponent(PlayerComponent.class);
        if (pc == null || pc.state == PlayerComponent.State.DEAD) return;

        pc.health -= 10;
        if (pc.health <= 0) {
            pc.state = PlayerComponent.State.DEAD;
            Gdx.app.log("AttackContactReceiver", "Player has died!");
        } else {
            pc.state     = PlayerComponent.State.HURT;
            pc.hurtTimer = pc.hurtDuration;
            pc.isHurt    = true;
            Gdx.app.log("AttackContactReceiver", "Player was hurt! HP=" + pc.health);
        }
    }
}
