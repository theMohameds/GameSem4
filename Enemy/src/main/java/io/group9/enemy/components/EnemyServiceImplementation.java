package io.group9.enemy.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import data.util.CoreResources;
import io.group9.enemy.ai.EnemyState;
import services.enemy.IEnemyService;

public class EnemyServiceImplementation implements IEnemyService {
    private Entity enemyEntity;
    private Body enemyBody;
    private int health;
    private final ComponentMapper<EnemyComponent> mapper = ComponentMapper.getFor(EnemyComponent.class);

    @Override public int getHealth() { return health; }
    @Override public void setHealth(int hp) { health = hp; }
    @Override public Entity getEnemyEntity() { return enemyEntity; }
    @Override public void setEnemyEntity(Entity e) { enemyEntity = e; }
    @Override public Body getEnemyBody() { return enemyBody; }
    @Override public void setEnemyBody(Body b) { enemyBody = b; }
    @Override
    public void resetForRound(Vector2 spawnPoint, boolean died) {
        setHealth(100);
        EnemyComponent c = mapper.get(enemyEntity);

        c.health = 100;
        c.jumpsLeft = c.maxJumps;
        c.attacking = false;
        c.attackRequested= false;
        c.attackTimer = 0f;
        c.attackCooldownTimer = 0f;
        c.isHurt = false;
        c.hurtTimer = 0f;
        c.reactionTimer = 0f;
        c.animTime = 0f;
        c.groundContacts = died ? 0 : 1;
        c.wasGrounded = !died;
        c.facingLeft = true;
        c.needsFreeze = false;
        c.state = EnemyState.IDLE;

        Body b = c.body;
        b.setType(BodyDef.BodyType.DynamicBody);
        b.setGravityScale(1f);
        b.setLinearVelocity(0f, 0f);
        b.setAngularVelocity(0f);
        b.setSleepingAllowed(false);
        b.setAwake(true);
        b.setTransform(spawnPoint.x / CoreResources.PPM, spawnPoint.y / CoreResources.PPM, 0f);
    }

    @Override
    public void freezeForRound() {
        EnemyComponent c = mapper.get(enemyEntity);
        c.needsFreeze = true;
    }
}
