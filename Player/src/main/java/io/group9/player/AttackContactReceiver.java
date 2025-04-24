package io.group9.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.ContactReceiver;

public class AttackContactReceiver implements ContactReceiver {
    @Override
    public void beginContact(Contact contact) {
        Object fixDataA = contact.getFixtureA().getUserData();
        Object fixDataB = contact.getFixtureB().getUserData();

        boolean aIsPlayerAttack   = "playerAttack".equals(fixDataA);
        boolean bIsPlayerAttack   = "playerAttack".equals(fixDataB);

        boolean aIsEnemyHurtbox  = "enemyHurtbox".equals(fixDataA);
        boolean bIsEnemyHurtbox  = "enemyHurtbox".equals(fixDataB);

        // Check collisions:
        if (aIsPlayerAttack && bIsEnemyHurtbox) {
            Gdx.app.log("AttackContactReceiver", "Enemy was hit by player's attack (case A)!");
        } else if (bIsPlayerAttack && aIsEnemyHurtbox) {
            Gdx.app.log("AttackContactReceiver", "Enemy was hit by player's attack (case B)!");
        }
    }

    @Override
    public void endContact(Contact contact) { }
}

