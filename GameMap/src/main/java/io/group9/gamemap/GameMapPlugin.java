package io.group9.gamemap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import io.group9.CoreResources;
import plugins.GameMapProvider;
import io.group9.gamemap.system.GameMapSystem;
import plugins.ECSPlugin;

import java.util.List;

public class GameMapPlugin implements ECSPlugin {
    private GameMapSystem mapSystem;

    @Override
    public void registerSystems(Engine engine) {
        Gdx.app.log("GameMapPlugin", "Initializing GameMapSystem…");
        mapSystem = new GameMapSystem(
            CoreResources.getWorld(),
            "map/New4.tmx",
            2,
            CoreResources.getCamera()
        );
        engine.addSystem(mapSystem);

        // Register provider via the expanded interface:
        CoreResources.setGameMapProvider(new GameMapProvider() {
            @Override
            public List<Rectangle> getMergedWorldRectangles() {
                return mapSystem.getMergedWorldRectangles();
            }
            @Override
            public int getLayerWidth() {
                return mapSystem.getLayerWidth();
            }
            @Override
            public int getLayerHeight() {
                return mapSystem.getLayerHeight();
            }
            @Override
            public float getCellSizeMeters() {
                return mapSystem.getCellSizeMeters();
            }
        });
    }

    @Override
    public void createEntities(Engine engine) {
        // no-op
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
