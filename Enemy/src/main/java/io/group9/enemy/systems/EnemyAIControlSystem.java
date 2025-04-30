package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.pathfinding.AStar;
import io.group9.enemy.pathfinding.PathGraph;
import io.group9.enemy.pathfinding.PathGraphVisualizer;
import io.group9.enemy.pathfinding.PathNode;

import java.util.ArrayList;
import java.util.List;

public class EnemyAIControlSystem extends EntitySystem {

    private ImmutableArray<Entity> enemies;
    private final float cellSize;
    private final Vector2 playerPos = new Vector2();
    private static final float SMOOTHING      = 800f;
    private static final float BLOCKED_VX     = 0.2f;
    private static final float JUMP_HEIGHT_TH = 0.3f;
    private List<PathNode> lastPrintedPath = null;

    public EnemyAIControlSystem(float cellSize) {
        this.cellSize = cellSize;
    }

    @Override public void addedToEngine(Engine eng) {
        enemies = eng.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        if (CoreResources.isRoundFrozen()) return;

        playerPos.set(CoreResources.getPlayerBody().getPosition());

        for (Entity ent : enemies) {
            EnemyComponent ec = ent.getComponent(EnemyComponent.class);
            if (ec.state == EnemyState.DEAD) continue;

            tickCooldown(ec, dt);
            Vector2 pos = ec.body.getPosition();

            // --- Pathfinding section ---
            List<Vector2> positions = CoreResources.getNodePositions();
            List<PathNode> allNodes = new ArrayList<>();
            for (Vector2 position : positions) {
                allNodes.add(new PathNode(position.x, position.y));
            }

            PathNode start = new PathNode(pos.x * CoreResources.PPM, pos.y * CoreResources.PPM);
            PathNode goal = new PathNode(playerPos.x * CoreResources.PPM, playerPos.y * CoreResources.PPM);
            PathGraph graph = new PathGraph();
            graph.addNode(start);
            graph.addNode(goal);
            for (PathNode node : allNodes) graph.addNode(node);
            graph.connectNodes();

            PathGraphVisualizer visualizer = new PathGraphVisualizer(graph, CoreResources.getCamera(), 1 / CoreResources.PPM);
            visualizer.render();

            AStar astar = new AStar();
            AStar.Heuristic heuristic = (node, g) -> {
                double dxh = node.x - g.x;
                double dyh = node.y - g.y;
                return Math.sqrt(dxh * dxh + dyh * dyh);
            };

            List<PathNode> newPath = astar.aStarSearch(start, goal, heuristic);

            // Check if the new path is different from the current one
            if (!pathsEqual(newPath, ec.currentPath)) {
                ec.currentNode = 0; // Reset progress only if the path changed
                ec.currentPath = newPath; // Store the new path
            }

            // Use the stored path for movement
            List<PathNode> path = ec.currentPath;

            if (path != null && !path.isEmpty()) {
                visualizer.renderPath(path);
            }

            // Always process movement (even if path is null/empty)
            handleJump(ec, path, dt);
            moveAlongHorizontalPath(ec, path, dt);
            // Debug print (optional)
            if (!pathsEqual(path, lastPrintedPath)) {
                lastPrintedPath = path;
                // printPathDebug(path);
            }
        }
    }

    private void handleJump(EnemyComponent ec, List<PathNode> path, float dt) {
        if (ec == null || ec.body == null || path == null || path.isEmpty()) {
            if (ec != null && ec.body != null) {
                ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
            }
            return;
        }

        // Prevent out-of-bounds access
        if (ec.currentNode >= path.size()) {
            return;
        }

        Vector2 currentPos = ec.body.getPosition();
        PathNode node = path.get(ec.currentNode);
        Vector2 target = new Vector2(node.x / CoreResources.PPM, node.y / CoreResources.PPM);
        Vector2 toTarget = target.cpy().sub(currentPos);

        if (toTarget.y > 15f / CoreResources.PPM) {
            doJump(ec, ec.body.getLinearVelocity().x, ec.FIRST_JUMP_VELOCITY, EnemyState.JUMP);
            System.out.println("Jump");
        }
    }

    private void moveAlongHorizontalPath(EnemyComponent ec, List<PathNode> path, float dt) {
        // 1. Check for invalid cases (no enemy, no physics body, or no path)
        if (ec == null || ec.body == null || path == null || path.isEmpty()) {
            if (ec != null && ec.body != null) {
                ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
            }
            return;
        }

        // 2. If currentNode is out of bounds, STOP and return
        if (ec.currentNode >= path.size()) {
            ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
            return; // Stop permanently
        }

        Vector2 currentPos = ec.body.getPosition();
        float deadZone = 1.5f; // Meters (physics units)
        ec.facingLeft = ec.body.getLinearVelocity().x < 0;

        PathNode node = path.get(ec.currentNode);
        Vector2 target = new Vector2(node.x / CoreResources.PPM, node.y / CoreResources.PPM);
        Vector2 toTarget = target.cpy().sub(currentPos);
        float distance = toTarget.len();

        // 3. If close enough to current node, move to next
        if (distance < deadZone) {
            ec.currentNode++;

            // 4. If no more nodes, stop permanently
            if (ec.currentNode >= path.size()) {
                ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
                return;
            }

            // Update target to next node
            node = path.get(ec.currentNode);
            target.set(node.x / CoreResources.PPM, node.y / CoreResources.PPM);
        } else {
            // Smoothly adjust velocity near the target
            Vector2 desiredVel = toTarget.scl(ec.maxLinearAcceleration);
            float vx = MathUtils.clamp(desiredVel.x, -ec.maxLinearSpeed, ec.maxLinearSpeed);
            ec.body.setLinearVelocity(vx, ec.body.getLinearVelocity().y);
        }

    }
    private void printPathDebug(List<PathNode> path) {
        if (path != null && !path.isEmpty()) {
            System.out.println("Path found:");
            for (int i = 0; i < path.size() - 1; i++) {
                PathNode curr = path.get(i);
                PathNode next = path.get(i + 1);
                String move;
                if (next.requiresDoubleJump) {
                    move = "DOUBLE_JUMP";
                } else if (next.requiresJump) {
                    move = "JUMP";
                } else {
                    move = "RUN";
                }
                System.out.printf(
                    "  Step %d → (%.1f, %.1f) → (%.1f, %.1f) : %s%n",
                    i, curr.x, curr.y, next.x, next.y, move
                );
            }
            PathNode last = path.get(path.size() - 1);
            System.out.printf("  Final → (%.1f, %.1f)%n", last.x, last.y);
        } else {
            System.out.println("No path found!");
        }
    }

    private boolean pathsEqual(List<PathNode> p1, List<PathNode> p2) {
        float epsilon = 0.1f; // Adjust based on needed precision (e.g., 0.1 pixels)
        if (p1 == null && p2 == null) return true;
        if (p1 == null || p2 == null) return false;
        if (p1.size() != p2.size()) return false;
        for (int i = 0; i < p1.size(); i++) {
            PathNode n1 = p1.get(i);
            PathNode n2 = p2.get(i);
            if (Math.abs(n1.x - n2.x) > epsilon || Math.abs(n1.y - n2.y) > epsilon) {
                return false;
            }
        }
        return true;
    }

    private void doJump(EnemyComponent ec, float vx, float vy, EnemyState st) {
        ec.body.setLinearVelocity(vx, vy);
        ec.jumpsLeft--;
        ec.state = st;
        ec.animTime = 0f;
    }

    private void tickCooldown(EnemyComponent ec, float dt) {
        if (ec.attackCooldownTimer > 0f) {
            ec.attackCooldownTimer = Math.max(0f, ec.attackCooldownTimer - dt);
        }
    }
}
