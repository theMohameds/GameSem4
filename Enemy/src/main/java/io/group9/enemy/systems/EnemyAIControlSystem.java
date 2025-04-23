package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.group9.CoreResources;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.pathfinding.PathNode;


public class EnemyAIControlSystem extends EntitySystem {
    private ImmutableArray<Entity> enemies;
    private final float cellSize;
    private final Vector2 playerPos = new Vector2();

    // tuning constants
    private static final float SMOOTHING = 8f;
    private static final float BLOCKED_VX = 0.2f;
    private static final float JUMP_HEIGHT_TH = 0.3f;

    public EnemyAIControlSystem(float cellSize) {
        this.cellSize = cellSize;
    }

    @Override
    public void addedToEngine(Engine engine) {
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        playerPos.set(CoreResources.getPlayerBody().getPosition());

        for (Entity ent : enemies) {
            EnemyComponent ec = ent.getComponent(EnemyComponent.class);
            if (ec.state == EnemyState.DEAD) continue;

            // Update cooldowns
            tickCooldown(ec, dt);

            // Always face the player (sprite flip)
            float px = playerPos.x, ex = ec.body.getPosition().x;
            boolean shouldFaceLeft = ex > px;
            ec.facingLeft = shouldFaceLeft;

            // Compute distances
            Vector2 pos = ec.body.getPosition();
            float dx = Math.abs(px - pos.x);
            float dy = Math.abs(playerPos.y - pos.y);

            // 1) If currently attacking or hurt, do nothing else
            if (ec.state == EnemyState.ATTACK || ec.state == EnemyState.HURT) {
                // keep velocity zero so we don't slide
                ec.body.setLinearVelocity(0f, ec.body.getLinearVelocity().y);
                continue;
            }

            // 2) If within attack range (both axes) AND facing correctly -> start attack
            if (dx <= ec.attackRange && dy <= ec.attackRange && ec.attackCooldownTimer <= 0f) {
                ec.state = EnemyState.ATTACK;
                ec.attackRequested = true;
                ec.animTime = 0f;
                // zero horizontal motion
                ec.body.setLinearVelocity(0f, ec.body.getLinearVelocity().y);
                continue;
            }

            // 3) If close but not facing (dx<=range but we just flipped), idle to turn
            if (dx <= ec.attackRange) {
                ec.state = EnemyState.IDLE;
                ec.body.setLinearVelocity(0f, ec.body.getLinearVelocity().y);
                continue;
            }

            // 4) Otherwise, chase: RUN state + jump logic + smooth horizontal
            ec.state = EnemyState.RUN;
            handleJumps(ec);
            float targetVX = computeTargetVX(ec);
            smoothHorizontal(ec, dt, targetVX);
        }
    }

    private void handleJumps(EnemyComponent ec) {
        Vector2 vel      = ec.body.getLinearVelocity();
        boolean grounded = ec.isGrounded();
        boolean wasGnd   = ec.wasGrounded;

        if (grounded && !wasGnd) {
            ec.jumpsLeft = ec.maxJumps;
        }
        ec.wasGrounded = grounded;

        // skip if already attacking/hurt
        if (ec.state == EnemyState.ATTACK || ec.state == EnemyState.HURT) return;

        // 1) Ledge jump: if next path node is higher
        if (grounded && ec.jumpsLeft > 0 && ec.currentNode < ec.path.getCount()) {
            PathNode next = ec.path.get(ec.currentNode);
            float nodeY = (next.y + 0.5f) * cellSize;
            float dy = nodeY - ec.body.getPosition().y;
            if (dy > JUMP_HEIGHT_TH) {
                doJump(ec, vel.x, ec.FIRST_JUMP_VELOCITY, EnemyState.JUMP);
                return;
            }
        }

        // 2) Obstacle‐ahead jump
        float targetVX = computeTargetVX(ec);
        if (grounded && ec.jumpsLeft > 0 &&
            Math.abs(targetVX) > 0.1f &&
            Math.abs(vel.x) < BLOCKED_VX) {
            doJump(ec, vel.x, ec.FIRST_JUMP_VELOCITY, EnemyState.JUMP);
            return;
        }

        // 3) Double jump
        if (!grounded && ec.jumpsLeft > 0) {
            doJump(ec, vel.x, ec.DOUBLE_JUMP_VELOCITY, EnemyState.JUMP);
        }
    }

    private void doJump(EnemyComponent ec, float vx, float vy, EnemyState newState) {
        ec.body.setLinearVelocity(vx, vy);
        ec.jumpsLeft--;
        ec.state = newState;
        ec.animTime = 0f;
    }

    private void tickCooldown(EnemyComponent ec, float dt) {
        if (ec.attackCooldownTimer > 0f) {
            ec.attackCooldownTimer = Math.max(0f, ec.attackCooldownTimer - dt);
        }
    }

    private float computeTargetVX(EnemyComponent ec) {
        Vector2 pos = ec.body.getPosition();
        if (ec.currentNode < ec.path.getCount()) {
            PathNode pn = ec.path.get(ec.currentNode);
            float tx = (pn.x + 0.5f) * cellSize;
            float dx = tx - pos.x;
            if (Math.abs(dx) < cellSize * 0.2f) ec.currentNode++;
            return Math.signum(dx) * ec.maxLinearSpeed;
        } else {
            // hold one attackRange away
            float dir = pos.x < playerPos.x ? 1f : -1f;
            float fx = playerPos.x - dir * ec.attackRange;
            float dx = fx - pos.x;
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
}
