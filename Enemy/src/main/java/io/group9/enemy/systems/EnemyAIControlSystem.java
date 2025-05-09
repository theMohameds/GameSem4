package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.pathfinding.AStar;
import io.group9.enemy.pathfinding.PathGraph;
import io.group9.enemy.pathfinding.PathGraphVisualizer;
import io.group9.enemy.pathfinding.PathNode;
import locators.CameraServiceLocator;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;

import java.util.ArrayList;
import java.util.List;

public class EnemyAIControlSystem extends EntitySystem {

    private ImmutableArray<Entity> enemies;
    private final float cellSize;
    private final Vector2 playerPos = new Vector2();
    private static final float SMOOTHING      = 800f;
    private static final float JUMP_HEIGHT_TH = 0.0f;

    // Shared graph and nodes
    private static PathGraph sharedGraph;
    private static List<PathNode> sharedNodes;

    private final IPlayerService playerSvc = PlayerServiceLocator.get();
    private final OrthographicCamera cam = CameraServiceLocator.get().getCamera();


    public EnemyAIControlSystem(float cellSize) {
        this.cellSize = cellSize;
    }

    @Override
    public void addedToEngine(Engine eng) {
        enemies = eng.getEntitiesFor(Family.all(EnemyComponent.class).get());
        initializePathGraphIfNeeded();
    }

    private static void initializePathGraphIfNeeded() {
        if (sharedGraph != null && sharedNodes != null) return;
        sharedGraph = new PathGraph();
        sharedNodes = new ArrayList<>();
        for (Vector2 position : CoreResources.getNodePositions()) {
            PathNode node = new PathNode(position.x, position.y);
            sharedNodes.add(node);
            sharedGraph.addNode(node);
        }
        sharedGraph.connectNodes();
    }

    @Override
    public void update(float dt) {
        if (CoreResources.isRoundFrozen()) return;
        playerPos.set(playerSvc.getPlayerBody().getPosition());

        for (Entity ent : enemies) {
            EnemyComponent ec = ent.getComponent(EnemyComponent.class);
            if (ec.state == EnemyState.DEAD) continue;

            tickCooldown(ec, dt);
            Vector2 pos = ec.body.getPosition();

            // Build frame-specific graph including start/goal
            PathNode start = new PathNode(pos.x * CoreResources.PPM, pos.y * CoreResources.PPM);
            PathNode goal = new PathNode(playerPos.x * CoreResources.PPM, playerPos.y * CoreResources.PPM);

            PathGraph graph = new PathGraph();
            graph.addNode(start);
            graph.addNode(goal);
            for (PathNode node : sharedNodes) graph.addNode(node);
            graph.connectNodes();

            PathGraphVisualizer visualizer = new PathGraphVisualizer(graph,
                cam, 1f / CoreResources.PPM);
            visualizer.render();

            AStar astar = new AStar();
            AStar.Heuristic heuristic = (n, g) -> {
                double dx = n.x - g.x;
                double dy = n.y - g.y;
                return Math.sqrt(dx*dx + dy*dy);
            };

            List<PathNode> newPath = astar.aStarSearch(start, goal, heuristic);
            // Remove the artificial start node so movement begins toward the first true waypoint
            if (!newPath.isEmpty() && newPath.get(0).equals(start)) {
                newPath.remove(0);
            }

            if (!pathsEqual(newPath, ec.currentPath)) {
                ec.currentPath  = newPath;
                ec.currentNode  = 0;
                ec.lastJumpNode = -1;

                // Reset jumps only when grounded
                if (ec.isGrounded()) {
                    ec.jumpsLeft = 2;
                }
            }

            List<PathNode> path = ec.currentPath;
            if (path != null && !path.isEmpty()) visualizer.renderPath(path);

            handleJump(ec, path, dt);
            moveAlongHorizontalPath(ec, path, dt);
        }
    }

    private PathNode findClosestNode(float x, float y) {
        PathNode closest = null;
        float minDist = Float.MAX_VALUE;
        for (PathNode node : sharedNodes) {
            float dx = node.x - x;
            float dy = node.y - y;
            float dist = dx*dx + dy*dy;
            if (dist < minDist) {
                minDist = dist;
                closest = node;
            }
        }
        return closest;
    }

    private void handleJump(EnemyComponent ec, List<PathNode> path, float dt) {
        if (ec == null || ec.body == null || path == null || path.isEmpty()) return;
        if (ec.currentNode >= path.size()) return;
        if (ec.currentNode == ec.lastJumpNode) return;

        Vector2 pos   = ec.body.getPosition();
        PathNode node = path.get(ec.currentNode);
        float nodeY   = node.y / CoreResources.PPM;
        float dy      = nodeY - pos.y;
        if (dy <= JUMP_HEIGHT_TH) return;
        if (ec.jumpsLeft <= 0) return;

        float jumpVel = (ec.jumpsLeft == 2)
            ? EnemyComponent.FIRST_JUMP_VELOCITY
            : EnemyComponent.DOUBLE_JUMP_VELOCITY;

        doJump(ec, ec.body.getLinearVelocity().x, jumpVel, EnemyState.JUMP);
        ec.lastJumpNode = ec.currentNode;
    }

    private void moveAlongHorizontalPath(EnemyComponent ec, List<PathNode> path, float dt) {
        if (ec == null || ec.body == null || path == null || path.isEmpty()) {
            ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
            return;
        }
        int lastIdx = path.size() - 1;
        if (ec.currentNode > lastIdx) {
            ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
            return;
        }

        Vector2 pos   = ec.body.getPosition();
        float NormalNodeDeadZone = 0.5f;
        float endDeadZone = 1.5f;
        PathNode node = path.get(ec.currentNode);
        Vector2 target = new Vector2(node.x / CoreResources.PPM,
            node.y / CoreResources.PPM);
        Vector2 toTarget = target.cpy().sub(pos);
        float dist = toTarget.len();
        float maxStep = ec.maxLinearSpeed * dt;

        // advance intermediate nodes
        while (ec.currentNode < lastIdx && (dist < NormalNodeDeadZone || maxStep >= dist)) {
            ec.currentNode++;
            ec.lastJumpNode = -1;
            node = path.get(ec.currentNode);
            target.set(node.x / CoreResources.PPM, node.y / CoreResources.PPM);
            toTarget = target.cpy().sub(pos);
            dist = toTarget.len();
        }

        // final node stop
        if (ec.currentNode == lastIdx && (dist < endDeadZone || maxStep >= dist)) {
            ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
            return;
        }

        // If jump direction is locked, respect it until grounded
        float vx;
        if (ec.jumpDirectionLocked != null && !ec.isGrounded()) {
            vx = ec.jumpDirectionLocked * ec.maxLinearSpeed;
        } else {
            float dirX = Math.signum(toTarget.x);
            vx = dirX * ec.maxLinearSpeed;
            ec.jumpDirectionLocked = null; // unlock direction after moving
        }

        ec.body.setLinearVelocity(vx, ec.body.getLinearVelocity().y);
        ec.facingLeft = vx < 0;

        if (ec.isGrounded() && ec.jumpDirectionLocked != null) {
            ec.jumpDirectionLocked = null; // unlock when grounded
        }
    }
    /*private void moveAlongHorizontalPath(EnemyComponent ec, List<PathNode> path, float dt) {
    if (ec == null || ec.body == null || path == null || path.isEmpty()) {
        ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
        return;
    }

    int lastIdx = path.size() - 1;
    if (ec.currentNode > lastIdx) {
        ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
        return;
    }

    Vector2 pos = ec.body.getPosition();
    float NormalNodeDeadZone = 0.5f;
    float endDeadZone = 1.5f;
    PathNode node = path.get(ec.currentNode);
    Vector2 target = new Vector2(node.x / CoreResources.PPM, node.y / CoreResources.PPM);
    Vector2 toTarget = target.cpy().sub(pos);
    float dist = toTarget.len();
    float maxStep = ec.maxLinearSpeed * dt;

    // Advance intermediate nodes
    while (ec.currentNode < lastIdx && (dist < NormalNodeDeadZone || maxStep >= dist)) {
        ec.currentNode++;
        ec.lastJumpNode = -1;
        node = path.get(ec.currentNode);
        target.set(node.x / CoreResources.PPM, node.y / CoreResources.PPM);
        toTarget = target.cpy().sub(pos);
        dist = toTarget.len();
    }

    // Final node stop
    if (ec.currentNode == lastIdx && (dist < endDeadZone || maxStep >= dist)) {
        ec.body.setLinearVelocity(0, ec.body.getLinearVelocity().y);
        return;
    }

    // Horizontal movement even when in the air
    float vx;
    if (ec.isGrounded()) {
        float dirX = Math.signum(toTarget.x);
        vx = dirX * ec.maxLinearSpeed;
    } else {
        // If airborne, move towards the player horizontally, even in the air
        float dirX = Math.signum(toTarget.x);
        vx = dirX * ec.maxLinearSpeed;
    }

    ec.body.setLinearVelocity(vx, ec.body.getLinearVelocity().y);
    ec.facingLeft = vx < 0;

    if (ec.isGrounded() && ec.jumpDirectionLocked != null) {
        ec.jumpDirectionLocked = null; // Unlock when grounded
        }
    }
     */

    private boolean pathsEqual(List<PathNode> p1, List<PathNode> p2) {
        if (p1 == p2) return true;
        if (p1 == null || p2 == null || p1.size() != p2.size()) return false;
        float eps = 0.1f;
        for (int i = 0; i < p1.size(); i++) {
            PathNode n1 = p1.get(i), n2 = p2.get(i);
            if (Math.abs(n1.x - n2.x) > eps || Math.abs(n1.y - n2.y) > eps)
                return false;
        }
        return true;
    }

    private void doJump(EnemyComponent ec, float vx, float vy, EnemyState st) {
        ec.body.setLinearVelocity(vx, vy);
        ec.jumpsLeft--;
        ec.state = st;
        ec.animTime = 0f;
        ec.jumpDirectionLocked = Math.signum(vx); // lock the direction during jump
    }

    private void tickCooldown(EnemyComponent ec, float dt) {
        if (ec.attackCooldownTimer > 0f) {
            ec.attackCooldownTimer = Math.max(0f, ec.attackCooldownTimer - dt);
        }
    }
}
