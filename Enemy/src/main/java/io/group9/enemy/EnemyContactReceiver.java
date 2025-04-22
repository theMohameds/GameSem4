package io.group9.enemy;

import io.group9.ContactReceiver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.ai.EnemyState;

public class EnemyContactReceiver implements ContactReceiver {

    @Override
    public void beginContact(Contact contact) {
        Object a = contact.getFixtureA().getUserData();
        Object b = contact.getFixtureB().getUserData();

        /* ---- ground sensor ---- */
        if ("footSensor".equals(a) || "footSensor".equals(b)) {
            EnemyComponent ec = ("footSensor".equals(a))
                ? (EnemyComponent) contact.getFixtureA().getBody().getUserData()
                : (EnemyComponent) contact.getFixtureB().getBody().getUserData();
            if (ec != null) {
                ec.groundContacts++;
                // on first contact with ground, reset jumps
                if (ec.groundContacts == 1) {
                    ec.jumpsLeft = ec.maxJumps;
                }
                // reset reaction delay if newly grounded
                if (!ec.wasGrounded) {
                    ec.reactionTimer = 0f;
                }
            }
        }

        /* ---- player attack vs hurtbox ---- */
        boolean aAtk  = "playerAttack".equals(a);
        boolean bAtk  = "playerAttack".equals(b);
        boolean aHurt = "enemyHurtbox".equals(a);
        boolean bHurt = "enemyHurtbox".equals(b);

        if (aAtk && bHurt) {
            hit((EnemyComponent) contact.getFixtureB().getBody().getUserData());
        }
        else if (bAtk && aHurt) {
            hit((EnemyComponent) contact.getFixtureA().getBody().getUserData());
        }
    }

    @Override
    public void endContact(Contact contact) {
        Object a = contact.getFixtureA().getUserData();
        Object b = contact.getFixtureB().getUserData();

        if ("footSensor".equals(a) || "footSensor".equals(b)) {
            EnemyComponent ec = ("footSensor".equals(a))
                ? (EnemyComponent) contact.getFixtureA().getBody().getUserData()
                : (EnemyComponent) contact.getFixtureB().getBody().getUserData();
            if (ec != null) {
                ec.groundContacts--;
                // start reaction delay when leaving all ground
                if (ec.groundContacts == 0) {
                    ec.reactionTimer = ec.reactionDelay;
                }
            }
        }
    }

    /** Apply damage or kill */
    private void hit(EnemyComponent ec) {
        if (ec == null || ec.state == EnemyState.DEAD) return;

        ec.health -= 10;
        if (ec.health <= 0) {
            ec.state      = EnemyState.DEAD;
            ec.needsFreeze= true;
            ec.animTime   = 0f;
            Gdx.app.log("EnemyContactReceiver", "Enemy killed");
        } else {
            ec.state      = EnemyState.HURT;
            ec.hurtTimer  = ec.hurtDuration;
            ec.isHurt     = true;
            ec.animTime   = 0f;
            Gdx.app.log("EnemyContactReceiver", "Enemy hurt");
        }
    }
}
