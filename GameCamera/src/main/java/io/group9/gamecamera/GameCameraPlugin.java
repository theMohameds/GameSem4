package io.group9.gamecamera;

import com.badlogic.ashley.core.Engine;
import io.group9.plugins.ECSPlugin;

public class GameCameraPlugin implements ECSPlugin {


    @Override
    public void registerSystems(Engine engine) {
        System.out.println("Registering GameCamera systems... ");

    }

    @Override
    public void createEntities(Engine engine) {
        System.out.println("Creating GameCamera entity...");

    }
}
