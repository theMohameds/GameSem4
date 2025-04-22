package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.pathfinding.GridGraph;
import io.group9.enemy.pathfinding.PathNode;

public class EnemyPathfindingSystem extends EntitySystem {
    private final GridGraph graph;
    private final IndexedAStarPathFinder<PathNode> pathFinder;
    private ImmutableArray<Entity> enemies;

    public EnemyPathfindingSystem(GridGraph graph) {
        this.graph = graph;
        this.pathFinder = new IndexedAStarPathFinder<>(graph, false);
    }

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        Vector2 playerPos = CoreResources.getPlayerBody().getPosition();
        float r = 1f; // get within 1 meter

        // compute a side‑by‑side goal position
        // enemies on left approach (player.x - r), on right (player.x + r)
        for (Entity e : enemies) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);
            ec.recalcTimer += dt;
            if (ec.recalcTimer < ec.recalcInterval) continue;
            ec.recalcTimer = 0f;

            Vector2 pos = ec.body.getPosition();
            float dir = pos.x < playerPos.x ? 1f : -1f;
            // side‑by‑side target
            float goalWorldX = playerPos.x - dir * r;
            float goalWorldY = playerPos.y;

            int goalX = (int)(goalWorldX / graph.getCellSize());
            int goalY = (int)(goalWorldY / graph.getCellSize());
            PathNode goal = new PathNode(goalX, goalY);

            int startX = (int)(pos.x / graph.getCellSize());
            int startY = (int)(pos.y / graph.getCellSize());
            PathNode start = new PathNode(startX, startY);

            ec.path.clear();
            pathFinder.searchNodePath(
                start,
                goal,
                new Heuristic<PathNode>() {
                    @Override
                    public float estimate(PathNode node, PathNode end) {
                        return Math.abs(node.x - end.x) + Math.abs(node.y - end.y);
                    }
                },
                ec.path
            );
            ec.currentNode = 0;
        }
    }
}
