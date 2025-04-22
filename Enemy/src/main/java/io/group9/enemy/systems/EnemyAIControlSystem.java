package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import io.group9.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.pathfinding.GridGraph;
import io.group9.enemy.pathfinding.PathNode;

public class EnemyAIControlSystem extends EntitySystem {
    private final GridGraph graph;
    private final IndexedAStarPathFinder<PathNode> pathFinder;
    private ImmutableArray<Entity> enemies;
    private final Vector2 playerPos = new Vector2();

    // tuning constants
    private static final float SMOOTHING       = 8f;
    private static final float BLOCKED_VX      = 0.2f;
    private static final float JUMP_HEIGHT_TH  = 0.3f;

    public EnemyAIControlSystem(GridGraph graph) {
        this.graph      = graph;
        this.pathFinder = new IndexedAStarPathFinder<>(graph, false);
    }

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        playerPos.set(CoreResources.getPlayerBody().getPosition());
        float cell = graph.getCellSize();

        for (Entity ent : enemies) {
            EnemyComponent ec = ent.getComponent(EnemyComponent.class);
            if (ec.state == EnemyState.DEAD) continue;

            // 1) Recompute path
            ec.recalcTimer += dt;
            if (ec.recalcTimer >= ec.recalcInterval) {
                ec.recalcTimer = 0f;
                recalcPath(ec, cell);
            }

            // 2) Decide jump needs
            handleJumps(ec, cell);

            // 3) Attack logic
            tickCooldown(ec, dt);
            tryAttack(ec);

            // 4) Horizontal smoothing
            float targetVX = computeTargetVX(ec, cell);
            smoothHorizontal(ec, dt, targetVX);
        }
    }

    private void recalcPath(EnemyComponent ec, float cell) {
        float dir    = ec.body.getPosition().x < playerPos.x ? 1f : -1f;
        float gx     = playerPos.x - dir * ec.attackRange;
        float gy     = playerPos.y;

        PathNode start = toNode(ec.body.getPosition(), cell);
        PathNode goal  = toNode(new Vector2(gx, gy), cell);

        ec.path.clear();
        pathFinder.searchNodePath(
            start, goal,
            (n, g) -> Math.abs(n.x - g.x) + Math.abs(n.y - g.y),
            ec.path
        );
        ec.currentNode = 0;
    }

    private void handleJumps(EnemyComponent ec, float cell) {
        Vector2 vel       = ec.body.getLinearVelocity();
        boolean grounded  = ec.isGrounded();
        boolean wasGround = ec.wasGrounded;

        // reset jumps on landing
        if (grounded && !wasGround) {
            ec.jumpsLeft = ec.maxJumps;
        }
        ec.wasGrounded = grounded;

        // skip while attacking or hurt
        if (ec.attacking || ec.state == EnemyState.HURT) return;

        // 1) Path‐height based first jump:
        float posY = ec.body.getPosition().y;
        if (ec.currentNode < ec.path.getCount() && grounded && ec.jumpsLeft > 0) {
            PathNode next = ec.path.get(ec.currentNode);
            float nodeY = (next.y + 0.5f) * cell;
            if (nodeY - posY > JUMP_HEIGHT_TH) {
                ec.body.setLinearVelocity(vel.x, EnemyComponent.FIRST_JUMP_VELOCITY);
                ec.jumpsLeft--;
                ec.state    = EnemyState.JUMP;
                ec.animTime = 0f;
                return;
            }
        }

        // 2) Obstacle‐ahead first jump (blocked by wall)
        float targetVX = computeTargetVX(ec, cell);
        if (grounded && ec.jumpsLeft > 0
            && Math.abs(targetVX) > 0.1f
            && Math.abs(vel.x) < BLOCKED_VX) {
            ec.body.setLinearVelocity(vel.x, EnemyComponent.FIRST_JUMP_VELOCITY);
            ec.jumpsLeft--;
            ec.state    = EnemyState.JUMP;
            ec.animTime = 0f;
            return;
        }

        // 3) Double jump whenever airborne and still have jumps
        if (!grounded && ec.jumpsLeft > 0) {
            ec.body.setLinearVelocity(vel.x, EnemyComponent.DOUBLE_JUMP_VELOCITY);
            ec.jumpsLeft--;
            ec.state    = EnemyState.AIRSPIN;
            ec.animTime = 0f;
        }
    }

    private void tickCooldown(EnemyComponent ec, float dt) {
        if (ec.attackCooldownTimer > 0f) {
            ec.attackCooldownTimer = Math.max(0f, ec.attackCooldownTimer - dt);
        }
    }

    private void tryAttack(EnemyComponent ec) {
        if (ec.attacking || ec.attackRequested || ec.attackCooldownTimer > 0f) return;
        Vector2 pos = ec.body.getPosition();
        float dx = Math.abs(playerPos.x - pos.x);
        float dy = Math.abs(playerPos.y - pos.y);
        if (dx <= ec.attackRange && dy < 0.5f) {
            ec.attackRequested     = true;
            ec.state               = EnemyState.ATTACK;
            ec.animTime            = 0f;
            ec.attackCooldownTimer = ec.attackCooldown + ec.attackDuration;
        }
    }

    private float computeTargetVX(EnemyComponent ec, float cell) {
        Vector2 pos = ec.body.getPosition();
        if (ec.currentNode < ec.path.getCount()) {
            PathNode pn = ec.path.get(ec.currentNode);
            float tx = (pn.x + .5f) * cell;
            float dx = tx - pos.x;
            if (Math.abs(dx) < cell * 0.2f) ec.currentNode++;
            return Math.signum(dx) * ec.maxLinearSpeed;
        } else {
            float dir = pos.x < playerPos.x ? 1f : -1f;
            float fx  = playerPos.x - dir * ec.attackRange;
            float dx  = fx - pos.x;
            return Math.abs(dx) > 0.1f
                ? Math.signum(dx) * ec.maxLinearSpeed
                : 0f;
        }
    }

    private void smoothHorizontal(EnemyComponent ec, float dt, float targetVX) {
        Vector2 vel = ec.body.getLinearVelocity();
        float newVX = MathUtils.lerp(vel.x, targetVX, Math.min(dt * SMOOTHING, 1f));
        ec.body.setLinearVelocity(newVX, vel.y);
        ec.facingLeft = newVX < 0;
    }

    private PathNode toNode(Vector2 v, float cell) {
        return new PathNode((int)(v.x / cell), (int)(v.y / cell));
    }
}
