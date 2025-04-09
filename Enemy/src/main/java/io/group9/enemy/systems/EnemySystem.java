package io.group9.enemy.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import io.group9.enemy.components.EnemyComponent;

public class EnemySystem extends EntitySystem {
    private ImmutableArray<Entity> enemies;

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : enemies) {
            EnemyComponent ec = entity.getComponent(EnemyComponent.class);

            if (ec.state == EnemyComponent.State.HURT) {
                ec.hurtTimer -= deltaTime;
                if (ec.hurtTimer <= 0f) {
                    ec.state = EnemyComponent.State.IDLE;
                }
            }
        }
    }
}



