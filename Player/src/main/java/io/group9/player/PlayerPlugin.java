package io.group9.player;

import com.badlogic.ashley.core.Engine;
import io.group9.player.system.PlayerInputSystem;
import io.group9.player.system.PlayerMovementSystem;
import plugins.ECSPlugin;

public class PlayerPlugin implements ECSPlugin {

    @Override
    public void registerSystems(Engine engine) {
        System.out.println("Registering Player systems...");
        engine.addSystem(new PlayerInputSystem());
        engine.addSystem(new PlayerMovementSystem());
    }

    @Override
    public void createEntities(Engine engine) {
        System.out.println("Creating Player entity...");
        // Optionally, create the player entity here if needed
    }

    @Override
    public int getPriority() {
        return 2;
    }
}

