package services.enemy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class NoopEnemyService implements IEnemyService {
    @Override public int getHealth() { return 0; }
    @Override public void setHealth(int hp) { }
    @Override public Entity getEnemyEntity() { return null; }
    @Override public void   setEnemyEntity(Entity e){ }
    @Override public Body getEnemyBody() { return null; }
    @Override public void setEnemyBody(Body b) { }
    @Override
    public void resetForRound(Vector2 spawnPoint, boolean died) { }
    @Override
    public void freezeForRound() { }
}
