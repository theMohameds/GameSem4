package io.group9.player;

import com.badlogic.gdx.Gdx;
import io.group9.ContactReceiver;
import com.badlogic.gdx.physics.box2d.Contact;

public class AttackContactReceiver implements ContactReceiver {
    @Override
    public void beginContact(Contact contact) {
        // Retrieve the user data from both bodies involved in the contact.
        Object dataA = contact.getFixtureA().getBody().getUserData();
        Object dataB = contact.getFixtureB().getBody().getUserData();

        // Check for a collision between the player's attack sensor and an enemy's hurtbox.
        if ("playerAttack".equals(dataA) && "enemy".equals(dataB)) {
            // Here you would apply damage to the enemy.
            Gdx.app.log("AttackContactReceiver", "Enemy hit by player attack (case A)!");
            // e.g. cast enemy object and call enemy.applyDamage(damageAmount);
        } else if ("enemy".equals(dataA) && "playerAttack".equals(dataB)) {
            Gdx.app.log("AttackContactReceiver", "Enemy hit by player attack (case B)!");
            // Apply damage similarly.
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Optionally, handle the end of an attack contact.
    }

}
