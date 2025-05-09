package io.group9.gamecamera;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import io.group9.gamecamera.system.GameCameraSystem;
import locators.CameraServiceLocator;
import plugins.ECSPlugin;
import services.gameCamera.ICameraService;

public class GameCameraPlugin implements ECSPlugin {
    private GameCameraSystem cameraSystem;

    @Override
    public void registerSystems(Engine engine) {
        Gdx.app.log("GameCameraPlugin", "Registering Camera System");
        // For a 640x360 resolution with PPM 16, viewport = 40 x 22.5 world units.
        cameraSystem = new GameCameraSystem(40, 22.5f, 20, 13f, true);
        ICameraService camSvc = CameraServiceLocator.get();
        camSvc.setCamera(cameraSystem.getCamera());
        engine.addSystem(cameraSystem);
    }

    @Override public void createEntities(Engine engine) { }
    @Override public int getPriority() { return 0; }
}
