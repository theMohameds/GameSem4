package io.group9.enemy.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.group9.enemy.ai.EnemyState;
import io.group9.enemy.pathfinding.PathNode;

import java.util.List;

public class EnemyComponent implements Component {

    // Physics handles
    public Body body;
    public Fixture footSensor;
    public Fixture hurtboxSensor;
    public Fixture attackSensor;

    // AI wrappers
    public EnemyState state = EnemyState.IDLE;

    // Steering caps
    public float maxLinearSpeed = 14f;
    public float boundingRadius = 0.4f;

    // Combat & Attack
    public int health = 100;
    public float attackDuration = 0.3f;
    public float attackTimer = 0f;
    public boolean attackRequested = false;
    public boolean attacking = false;
    public float attackCooldownTimer = 0f;

    // Hurt
    public float hurtDuration = 0.264f;
    public float hurtTimer = 0f;
    public boolean isHurt = false;

    // Jump stats
    public final int maxJumps = 2;
    public int jumpsLeft = maxJumps;
    public static final float FIRST_JUMP_VELOCITY = 23f;
    public Float jumpDirectionLocked = null;
    public static final float DOUBLE_JUMP_VELOCITY = 23f;

    // Path-finding
    public GraphPath<PathNode> path;
    public int currentNode = 0;
    public int lastJumpNode = -1;

    // Reaction delay (AI)
    public float reactionDelay = 0f;
    public float reactionTimer = 0f;

    // Facing & animation
    public boolean facingLeft = false;
    public float animTime = 0f;
    public boolean needsFreeze = false;

    // Ground detection
    public int groundContacts = 0;
    public boolean wasGrounded = false;
    public boolean isGrounded() {
        return groundContacts > 0;
    }

    // Misc settings
    public float attackRange = 1.3f;

    // Attack sensor dimensions (pixels)
    public float sensorW = 16f;
    public float sensorH = 30f;

    public List<PathNode> currentPath = null;

}
