package io.group9.enemy;

import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.enemy.systems.EnemyAIControlSystem;
import io.group9.enemy.systems.EnemyAttackSystem;
import io.group9.enemy.systems.EnemyAnimationRenderer;
import io.group9.enemy.systems.EnemyStateSystem;
import plugins.ECSPlugin;

public class EnemyPlugin implements ECSPlugin {
    private boolean spawned = false;

    @Override
    public void registerSystems(Engine engine) {
        float cellSize = 64f / CoreResources.PPM;

        engine.addSystem(new EnemyAIControlSystem(cellSize));
        engine.addSystem(new EnemyStateSystem());
        engine.addSystem(new EnemyAttackSystem());
        engine.addSystem(new EnemyAnimationRenderer());

        CoreResources.getContactDispatcher()
            .addReceiver(new EnemyContactReceiver());
    }

    @Override
    public void createEntities(Engine engine) {
        if (!spawned) {
            EnemyFactory.spawn(engine);
            spawned = true;
        }
    }

    @Override
    public int getPriority() {
        return 4;
    }
}
