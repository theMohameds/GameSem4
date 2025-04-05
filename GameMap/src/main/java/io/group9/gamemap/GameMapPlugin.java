package io.group9.gamemap;

import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.gamemap.system.GameMapSystem;
import plugins.ECSPlugin;

public class GameMapPlugin implements ECSPlugin {

    private GameMapSystem gameMapSystem;


    @Override
    public void registerSystems(Engine engine) {
        System.out.println("Registering GameMap systems...");


        gameMapSystem = new GameMapSystem(CoreResources.getWorld(), "map/New4.tmx",
            2, CoreResources.getCamera());
        engine.addSystem(gameMapSystem);
    }

    @Override
    public void createEntities(Engine engine) {
        System.out.println("Creating GameMap entity...");


    }

    @Override
    public int getPriority() {
        return 1;
    }

}
