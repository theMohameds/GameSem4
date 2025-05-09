package io.group9.enemy.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import services.enemy.IEnemyService;

public class EnemyServiceImplementation implements IEnemyService {
    private Entity enemyEntity;
    private Body enemyBody;
    private int health;

    @Override public int getHealth() { return health; }
    @Override public void setHealth(int hp) { health = hp; }
    @Override public Entity getEnemyEntity() { return enemyEntity; }
    @Override public void setEnemyEntity(Entity e) { enemyEntity = e; }
    @Override public Body getEnemyBody() { return enemyBody; }
    @Override public void setEnemyBody(Body b) { enemyBody = b; }
}
