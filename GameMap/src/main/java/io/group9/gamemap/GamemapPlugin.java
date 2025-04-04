package io.group9.gamemap;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.gamemap.system.GameMapSystem;
import io.group9.plugins.ECSPlugin;
import com.badlogic.ashley.core.Entity;

public class GamemapPlugin implements ECSPlugin {

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
