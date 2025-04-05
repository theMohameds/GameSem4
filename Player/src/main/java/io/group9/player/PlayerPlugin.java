package io.group9.player;

import com.badlogic.ashley.core.Engine;
import plugins.ECSPlugin;

public class PlayerPlugin implements ECSPlugin {

    @Override
    public void registerSystems(Engine engine) {
        System.out.println("Registering Player systems...");
    }

    @Override
    public void createEntities(Engine engine) {
        System.out.println("Creating Player entity...");
    }

    @Override
    public int getPriority() {
        return 2;
    }

}
