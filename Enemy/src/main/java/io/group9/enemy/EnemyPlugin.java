package io.group9.enemy;

import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.enemy.pathfinding.GridGraph;
import io.group9.enemy.systems.*;
import plugins.ECSPlugin;

public class EnemyPlugin implements ECSPlugin {
    private boolean spawned = false;

    @Override
    public void registerSystems(Engine engine) {
        float cellSize = 64f / CoreResources.PPM;
        GridGraph grid = new GridGraph(100, 20, cellSize);

        engine.addSystem(new EnemyPathfindingSystem(grid));
        engine.addSystem(new EnemyAIControlSystem(grid));
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
