package io.group9.enemy.pathfinding;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import io.group9.CoreResources;
import plugins.ECSPlugin;
import io.group9.enemy.systems.EnemyPathfindingSystem;


public class PathfindingPlugin implements ECSPlugin {
    private static final String MAP_PATH = "map/New4.tmx";
    private static final int COLLISION_LAYER_IDX = 2;

    @Override
    public void registerSystems(Engine engine) {
        // 1) Load the map and collision layer
        TiledMap map = new TmxMapLoader().load(MAP_PATH);
        TiledMapTileLayer collisionLayer =
            (TiledMapTileLayer)map.getLayers().get(COLLISION_LAYER_IDX);

        int cols = collisionLayer.getWidth();
        int rows = collisionLayer.getHeight();
        float tilePx = collisionLayer.getTileWidth();
        float cellSize = tilePx / CoreResources.PPM;

        // 2) Build terrainCosts[y][x]: 1f = walkable, ∞ = blocked
        float[][] terrainCosts = new float[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Cell cell = collisionLayer.getCell(x, y);
                terrainCosts[y][x] = (cell != null && cell.getTile() != null)
                    ? Float.POSITIVE_INFINITY   // blocked
                    : 1.0f; // normal ground
            }
        }

        // 3) Register the advanced A* system
        engine.addSystem(new EnemyPathfindingSystem(terrainCosts, cellSize));
    }

    @Override
    public void createEntities(Engine engine) {
        // No map‐specific entities to spawn here
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
