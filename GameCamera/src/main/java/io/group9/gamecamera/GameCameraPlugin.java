package io.group9.gamecamera;

import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.gamecamera.system.GameCameraSystem;
import plugins.ECSPlugin;

public class GameCameraPlugin implements ECSPlugin {

    private GameCameraSystem gameCameraSystem;

    @Override
    public void registerSystems(Engine engine) {
        System.out.println("Registering GameCameraSystem systems... ");

        gameCameraSystem = new GameCameraSystem(640, 360, 0, 0, true);
        CoreResources.setCamera(gameCameraSystem.getCamera());

        engine.addSystem(gameCameraSystem);


    }

    @Override
    public void createEntities(Engine engine) {
        System.out.println("Creating GameCameraSystem entity...");
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
