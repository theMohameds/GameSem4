package io.group9.enemy.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.BodyDef;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.ai.EnemyState;

public class EnemyStateSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private static final float UP   = 5.5f;
    private static final float DOWN = 5.5f;

    @Override
    public void addedToEngine(Engine eng) {
        entities = eng.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override
    public void update(float dt) {
        for (Entity e : entities) {
            EnemyComponent ec = e.getComponent(EnemyComponent.class);

            if (ec.state == EnemyState.DEAD && ec.needsFreeze) {
                ec.body.setLinearVelocity(0f, 0f);
                ec.body.setGravityScale(0f);
                ec.body.setType(BodyDef.BodyType.StaticBody);
                ec.needsFreeze = false;
            }
            if (ec.state == EnemyState.DEAD) continue;

            boolean nowGrounded = ec.isGrounded();
            if (nowGrounded && !ec.wasGrounded) {
                ec.jumpsLeft = ec.maxJumps;
            }
            ec.wasGrounded = nowGrounded;

            if (ec.state == EnemyState.HURT) {
                ec.hurtTimer -= dt;
                if (ec.hurtTimer <= 0f) {
                    ec.state    = EnemyState.IDLE;
                    ec.isHurt   = false;
                    ec.animTime = 0f;
                }
                continue;
            }

            if (ec.attacking) continue;

            if (nowGrounded) {
                ec.state = Math.abs(ec.body.getLinearVelocity().x) > 0.1f
                    ? EnemyState.RUN
                    : EnemyState.IDLE;
            } else {
                ec.state = ec.body.getLinearVelocity().y > 0
                    ? EnemyState.JUMP
                    : EnemyState.AIRSPIN;
            }

            ec.body.setGravityScale(
                ec.body.getLinearVelocity().y > 0 ? UP : DOWN
            );
        }
    }
}
