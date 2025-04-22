package io.group9.enemy.pathfinding;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Rectangle;
import io.group9.CoreResources;
import plugins.ECSPlugin;
import io.group9.enemy.systems.EnemyPathfindingSystem;

import java.util.List;

/**
 * Creates the navigation grid and registers the systems that use it.
 * Adjust width/height to match your Tiled map bounds.
 */
public class PathfindingPlugin implements ECSPlugin {

    private static final int GRID_W = 100;
    private static final int GRID_H = 20;

    private static final float CELL_SIZE = 64f / CoreResources.PPM;

    @Override
    public void registerSystems(Engine engine) {
        List<Rectangle> obstacles = gatherObstacleRects();

        GridGraph navGraph = new GridGraph(GRID_W, GRID_H, CELL_SIZE);


        engine.addSystem(new EnemyPathfindingSystem(navGraph));
    }

    @Override public void createEntities(Engine eng) { /* nothing */ }

    @Override public int getPriority() { return 3; }

    /* ------------------------------------------------------------------ */

    private List<Rectangle> gatherObstacleRects() {
        return java.util.Collections.emptyList();
    }

    @SuppressWarnings("unused")
    private void markBlocked(GridGraph g, List<Rectangle> rects) {
        for (Rectangle r : rects) {
            int gx = (int) (r.x / g.getCellSize());
            int gy = (int) (r.y / g.getCellSize());
            // Override GridGraph and implement isBlocked() with your own data.
        }
    }
}
