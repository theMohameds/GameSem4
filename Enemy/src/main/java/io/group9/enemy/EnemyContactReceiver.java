package io.group9.enemy;

import io.group9.ContactReceiver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import io.group9.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.ai.EnemyState;
import locators.InventoryServiceLocator;
import locators.PlayerServiceLocator;
import services.weapon.IInventoryService;
import services.weapon.IWeapon;
import services.player.IPlayerService;

import java.util.Optional;

public class EnemyContactReceiver implements ContactReceiver {

    @Override
    public void beginContact(Contact contact) {
        Object a = contact.getFixtureA().getUserData();
        Object b = contact.getFixtureB().getUserData();

        // Ground Sensor
        if ("footSensor".equals(a) || "footSensor".equals(b)) {
            EnemyComponent ec = ("footSensor".equals(a))
                ? (EnemyComponent) contact.getFixtureA().getBody().getUserData()
                : (EnemyComponent) contact.getFixtureB().getBody().getUserData();
            if (ec != null) {
                ec.groundContacts++;
                if (ec.groundContacts == 1) {
                    ec.jumpsLeft = ec.maxJumps;
                }

                if (!ec.wasGrounded) {
                    ec.reactionTimer = 0f;
                }
            }
        }

        // Attacks
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
                if (ec.groundContacts == 0) {
                    ec.reactionTimer = ec.reactionDelay;
                }
            }
        }
    }

    // Apply damage or kill
    private void hit(EnemyComponent ec) {
        if (ec == null || ec.state == EnemyState.DEAD) {
            return;
        }

        IInventoryService inv = InventoryServiceLocator.getInventoryService();
        IPlayerService playerSvc = PlayerServiceLocator.get();
        Optional<IWeapon> w = inv.getCurrentWeapon(playerSvc.getPlayerEntity());
        int damage = w.map(ws -> "Sword".equals(ws.getName()) ? 20 : 50).orElse(50);

        ec.health -= damage;
        CoreResources.setEnemyHealth(ec.health);

        if (ec.health <= 0) {
            ec.state      = EnemyState.DEAD;
            ec.needsFreeze= true;
            Gdx.app.log("EnemyContactReceiver", "Enemy killed");
        } else {
            ec.state      = EnemyState.HURT;
            ec.hurtTimer  = ec.hurtDuration;
            ec.isHurt     = true;
            Gdx.app.log("EnemyContactReceiver", "Enemy hurt for " + damage);
        }
    }
}
