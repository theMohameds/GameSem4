package io.group9.gamemap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import io.group9.CoreResources;
import io.group9.gamemap.system.GameMapSystem;
import plugins.ECSPlugin;

public class GameMapPlugin implements ECSPlugin {
    private GameMapSystem gameMapSystem;

    @Override
    public void registerSystems(Engine engine) {
        Gdx.app.log("GameMapPlugin", "Registering GameMapSystem...");
        // Adjust map path and collision layer index as needed.
        gameMapSystem = new GameMapSystem(CoreResources.getWorld(), "map/New4.tmx", 2, CoreResources.getCamera());
        engine.addSystem(gameMapSystem);
    }

    @Override
    public void createEntities(Engine engine) {
        Gdx.app.log("GameMapPlugin", "No explicit map entities to create.");
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
