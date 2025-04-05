package io.group9.gamemap;

import com.badlogic.ashley.core.Engine;
import io.group9.gamemap.system.GameMapSystem;
import plugins.ECSPlugin;

public class GameMapPlugin implements ECSPlugin {

    private GameMapSystem gameMapSystem;


    @Override
    public void registerSystems(Engine engine) {
        System.out.println("Registering GameMap systems...");

    }

    @Override
    public void createEntities(Engine engine) {
        System.out.println("Creating GameMap entity...");

    }

}
