package services.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public interface IPlayerService {
    int getHealth();

    void setHealth(int health);

    int getMaxHealth();

    void setMaxHealth(int maxHealth);

    Entity getPlayerEntity();

    void setPlayerEntity(Entity playerEntity);

    Body getPlayerBody();

    void setPlayerBody(Body playerBody);

    void resetForRound(Vector2 spawnPoint);

    void freezeForRound();
}
