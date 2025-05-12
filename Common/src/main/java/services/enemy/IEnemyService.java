package services.enemy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public interface IEnemyService {
    int getHealth();
    void setHealth(int hp);

    Entity getEnemyEntity();
    void setEnemyEntity(Entity e);

    Body getEnemyBody();
    void setEnemyBody(Body b);

    void resetForRound(Vector2 spawnPoint, boolean died);
    void freezeForRound();
}
