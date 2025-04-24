package io.group9.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.ContactReceiver;
import io.group9.enemy.components.EnemyComponent;

public class EnemyContactReceiver implements ContactReceiver {
    @Override
    public void beginContact(Contact contact) {
        Object fixDataA = contact.getFixtureA().getUserData();
        Object fixDataB = contact.getFixtureB().getUserData();

        boolean aIsPlayerAttack   = "playerAttack".equals(fixDataA);
        boolean bIsPlayerAttack   = "playerAttack".equals(fixDataB);
        boolean aIsEnemyHurtbox   = "enemyHurtbox".equals(fixDataA);
        boolean bIsEnemyHurtbox   = "enemyHurtbox".equals(fixDataB);

        // Case A: fixture A is player's attack and fixture B is enemy hurtbox.
        if (aIsPlayerAttack && bIsEnemyHurtbox) {
            EnemyComponent enemy = (EnemyComponent) contact.getFixtureB().getBody().getUserData();
            if (enemy != null && enemy.state != EnemyComponent.State.DEAD) {
                enemy.health -= 10;
                if (enemy.health <= 0) {
                    enemy.state = EnemyComponent.State.DEAD;
                    Gdx.app.log("EnemyContactReceiver", "Enemy has died (case A).");
                } else {
                    enemy.state = EnemyComponent.State.HURT;
                    enemy.hurtTimer = enemy.hurtDuration;
                    enemy.isHurt = true;
                    Gdx.app.log("EnemyContactReceiver", "Enemy was hit and is now hurt (case A).");
                }
            }
        }
        // Case B: fixture A is enemy hurtbox and fixture B is player's attack.
        else if (bIsPlayerAttack && aIsEnemyHurtbox) {
            EnemyComponent enemy = (EnemyComponent) contact.getFixtureA().getBody().getUserData();
            if (enemy != null && enemy.state != EnemyComponent.State.DEAD) {
                enemy.health -= 10;
                if (enemy.health <= 0) {
                    enemy.state = EnemyComponent.State.DEAD;
                    Gdx.app.log("EnemyContactReceiver", "Enemy has died (case B).");
                } else {
                    enemy.state = EnemyComponent.State.HURT;
                    enemy.hurtTimer = enemy.hurtDuration;
                    enemy.isHurt = true;
                    Gdx.app.log("EnemyContactReceiver", "Enemy was hit and is now hurt (case B).");
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Optionally add logic here if needed.
    }
}

