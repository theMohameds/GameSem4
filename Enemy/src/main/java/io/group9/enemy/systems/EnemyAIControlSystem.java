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
    private static final float SMOOTHING      = 8f;
    private static final float BLOCKED_VX     = 0.2f;
    private static final float JUMP_HEIGHT_TH = 0.3f;
    private List<PathNode> lastPrintedPath = null;

    public EnemyAIControlSystem(float cellSize) { this.cellSize = cellSize; }

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

            ec.facingLeft = ec.body.getPosition().x > playerPos.x;

            Vector2 pos = ec.body.getPosition();
            float dx = Math.abs(playerPos.x - pos.x);
            float dy = Math.abs(playerPos.y - pos.y);

            // --- Pathfinding section ---
            List<Vector2> positions = CoreResources.getNodePositions();
            List<PathNode> allNodes = new ArrayList<>();

            for (Vector2 position : positions) {
                allNodes.add(new PathNode(position.x, position.y));
            }

            PathNode start = new PathNode(ec.body.getPosition().x * CoreResources.PPM, ec.body.getPosition().y * CoreResources.PPM);
            PathNode goal = new PathNode(playerPos.x * CoreResources.PPM, playerPos.y * CoreResources.PPM);
            PathGraph graph = new PathGraph();
            graph.addNode(start);
            graph.addNode(goal);

            for (PathNode node : allNodes) {
                graph.addNode(node);
            }

            graph.connectNodes();
            PathGraphVisualizer visualizer = new PathGraphVisualizer(graph, CoreResources.getCamera(), 1/CoreResources.PPM);

            visualizer.render();

            AStar astar = new AStar();
            AStar.Heuristic heuristic = (node, g) -> {
                double dxh = node.x - g.x;
                double dyh = node.y - g.y;
                return Math.sqrt(dxh * dxh + dyh * dyh);
            };

            List<PathNode> path = astar.aStarSearch(start, goal, heuristic);

            if (path != null && !path.isEmpty()) {
                visualizer.renderPath(path);
            }
            // Check if path is different from lastPrintedPath
            if (!pathsEqual(path, lastPrintedPath)) {
                lastPrintedPath = path;

                if (path != null && !path.isEmpty()) {
                    System.out.println("Path found:");
                    for (int i = 0; i < path.size() - 1; i++) {
                        PathNode curr = path.get(i);
                        PathNode next = path.get(i + 1);

                        // determine move type by inspecting the flags on the *next* node
                        String move;
                        if (next.requiresDoubleJump) {
                            move = "DOUBLE_JUMP";
                        } else if (next.requiresJump) {
                            move = "JUMP";
                        } else {
                            move = "RUN";
                        }

                        System.out.printf(
                            "  Step %d → (%5.1f, %5.1f) → (%5.1f, %5.1f) : %s%n",
                            i,
                            curr.x, curr.y,
                            next.x, next.y,
                            move
                        );
                    }
                    // optionally, print the last node alone
                    PathNode last = path.get(path.size() - 1);
                    System.out.printf("  Final → (%5.1f, %5.1f)%n", last.x, last.y);
                } else {
                    System.out.println("No path found!");
                }
            }
        }
    }

    // Helper method to compare two paths
    private boolean pathsEqual(List<PathNode> p1, List<PathNode> p2) {
        if (p1 == null && p2 == null) return true;
        if (p1 == null || p2 == null) return false;
        if (p1.size() != p2.size()) return false;
        for (int i = 0; i < p1.size(); i++) {
            PathNode n1 = p1.get(i);
            PathNode n2 = p2.get(i);
            if (n1.x != n2.x || n1.y != n2.y) return false;
        }
        return true;
    }

    private void handleJumps(EnemyComponent ec) {
        Vector2 vel = ec.body.getLinearVelocity();
        boolean grounded = ec.isGrounded();
        if (grounded && !ec.wasGrounded) ec.jumpsLeft = ec.maxJumps;
        ec.wasGrounded = grounded;

        if (ec.state == EnemyState.ATTACK || ec.state == EnemyState.HURT) return;

        if (grounded && ec.jumpsLeft > 0 && ec.currentNode < ec.path.getCount()) {
            PathNode next = ec.path.get(ec.currentNode);
            float nodeY = (next.y + 0.5f) * cellSize;
            if (nodeY - ec.body.getPosition().y > JUMP_HEIGHT_TH) {
                doJump(ec, vel.x, ec.FIRST_JUMP_VELOCITY, EnemyState.JUMP);
                return;
            }
        }

        float targetVX = computeTargetVX(ec);
        if (grounded && ec.jumpsLeft > 0 &&
            Math.abs(targetVX) > 0.1f && Math.abs(vel.x) < BLOCKED_VX) {
            doJump(ec, vel.x, ec.FIRST_JUMP_VELOCITY, EnemyState.JUMP);
            return;
        }

        if (!grounded && ec.jumpsLeft > 0) {
            doJump(ec, vel.x, ec.DOUBLE_JUMP_VELOCITY, EnemyState.JUMP);
        }
    }
    private void doJump(EnemyComponent ec, float vx, float vy, EnemyState st) {
        ec.body.setLinearVelocity(vx, vy);
        ec.jumpsLeft--;
        ec.state = st;
        ec.animTime = 0f;
    }

    private void tickCooldown(EnemyComponent ec, float dt) {
        if (ec.attackCooldownTimer > 0f)
            ec.attackCooldownTimer = Math.max(0f, ec.attackCooldownTimer - dt);
    }
    private float computeTargetVX(EnemyComponent ec) {
        Vector2 pos = ec.body.getPosition();
        if (ec.currentNode < ec.path.getCount()) {
            PathNode pn = ec.path.get(ec.currentNode);
            float tx = (pn.x + 0.5f) * cellSize;
            float dx = tx - pos.x;
            if (Math.abs(dx) < cellSize * 0.2f) ec.currentNode++;
            return Math.signum(dx) * ec.maxLinearSpeed;
        }
        float dir = pos.x < playerPos.x ? 1f : -1f;
        float fx  = playerPos.x - dir * ec.attackRange;
        float dx  = fx - pos.x;
        return Math.abs(dx) > 0.1f ? Math.signum(dx) * ec.maxLinearSpeed : 0f;
    }
    private void smoothHorizontal(EnemyComponent ec,float dt,float tgt){
        Vector2 vel = ec.body.getLinearVelocity();
        float newVX = MathUtils.lerp(vel.x, tgt, Math.min(dt * SMOOTHING, 1f));
        ec.body.setLinearVelocity(newVX, vel.y);
        ec.facingLeft = newVX < 0;
    }
}
