package io.group9.enemy.plugins;

import com.badlogic.ashley.core.Engine;
import contact.IContactDispatcherService;
import data.util.CoreResources;
import io.group9.enemy.contactReceivers.EnemyContactReceiver;
import io.group9.enemy.systems.EnemyAIControlSystem;
import io.group9.enemy.systems.EnemyAttackSystem;
import io.group9.enemy.systems.EnemyAnimationRenderer;
import io.group9.enemy.systems.EnemyStateSystem;
import locators.ContactDispatcherLocator;
import plugins.ECSPlugin;

public class EnemyPlugin implements ECSPlugin {
    private boolean spawned = false;
    IContactDispatcherService dispatcher = ContactDispatcherLocator.get();

    @Override
    public void registerSystems(Engine engine) {
        float cellSize = 32f / CoreResources.PPM;
        engine.addSystem(new EnemyAIControlSystem(cellSize));
        engine.addSystem(new EnemyStateSystem());
        engine.addSystem(new EnemyAttackSystem());
        engine.addSystem(new EnemyAnimationRenderer());

        dispatcher.addReceiver(new EnemyContactReceiver());
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
