package io.group9.gamemap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import data.WorldProvider;
import locators.CameraServiceLocator;
import plugins.GameMapProvider;
import services.gameCamera.ICameraService;
import io.group9.gamemap.system.GameMapSystem;
import plugins.ECSPlugin;
import util.CoreResources;

import java.util.List;

public class GameMapPlugin implements ECSPlugin {
    private GameMapSystem mapSystem;

    @Override
    public void registerSystems(Engine engine) {
        Gdx.app.log("GameMapPlugin", "Initializing GameMapSystem…");

        World world = WorldProvider.getWorld();

        ICameraService camSvc = CameraServiceLocator.get();
        OrthographicCamera cam = camSvc.getCamera();
        if (cam == null) {
            Gdx.app.error("GameMapPlugin", "Camera not initialized before map plugin!");
        }

        mapSystem = new GameMapSystem(
            world,
            "map/mountain/mountains.tmx",
            cam
        );
        engine.addSystem(mapSystem);

        CoreResources.setGameMapProvider(new GameMapProvider() {
            @Override public List<Rectangle> getMergedWorldRectangles() {
                return mapSystem.getMergedWorldRectangles();
            }
            @Override public int getLayerWidth()    { return mapSystem.getLayerWidth(); }
            @Override public int getLayerHeight()   { return mapSystem.getLayerHeight(); }
            @Override public float getCellSizeMeters() {
                return mapSystem.getCellSizeMeters();
            }
        });
    }

    @Override public void createEntities(Engine engine) { }

    @Override public int getPriority() { return 1; }
}
