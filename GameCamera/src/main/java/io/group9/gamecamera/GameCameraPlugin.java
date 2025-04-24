package io.group9.gamecamera;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import io.group9.CoreResources;
import io.group9.gamecamera.system.GameCameraSystem;
import plugins.ECSPlugin;

public class GameCameraPlugin implements ECSPlugin {
    private GameCameraSystem cameraSystem;

    @Override
    public void registerSystems(Engine engine) {
        Gdx.app.log("GameCameraPlugin", "Registering Camera System");
        // For a 640x360 resolution with PPM 16, viewport = 40 x 22.5 world units.
        cameraSystem = new GameCameraSystem(40, 22.5f, 20, 11.25f, true);
        CoreResources.setCamera(cameraSystem.getCamera());
        engine.addSystem(cameraSystem);
    }

    @Override public void createEntities(Engine engine) { }
    @Override public int getPriority() { return 0; }
}
