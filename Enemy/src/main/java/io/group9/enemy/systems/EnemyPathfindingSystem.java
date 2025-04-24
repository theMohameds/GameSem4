package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import io.group9.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.pathfinding.AStar;
import io.group9.enemy.pathfinding.PathNode;
import com.badlogic.gdx.math.Vector2;
import java.util.List;


public class EnemyPathfindingSystem extends EntitySystem {
    private ImmutableArray<Entity> enemies;
    private final AStar astar;
    private final float cellSize;

    public EnemyPathfindingSystem(float[][] terrainCosts, float cellSize) {
        this.astar    = new AStar(terrainCosts);
        this.cellSize = cellSize;
    }

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        Vector2 playerPos = CoreResources.getPlayerBody().getPosition();
        int goalX = (int)(playerPos.x / cellSize);
        int goalY = (int)(playerPos.y / cellSize);

        for (Entity e : enemies) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);
            Vector2 pos = ec.body.getPosition();
            int sx = (int)(pos.x / cellSize);
            int sy = (int)(pos.y / cellSize);

            List<PathNode> raw = astar.findPath(sx, sy, goalX, goalY);

            ec.path.clear();
            for (PathNode n : raw) {
                ec.path.add(n);
            }
            ec.currentNode = 0;
        }
    }
}

