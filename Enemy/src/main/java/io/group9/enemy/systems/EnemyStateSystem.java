package io.group9.enemy.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.BodyDef;
import io.group9.enemy.components.EnemyComponent;
import io.group9.enemy.ai.EnemyState;

public class EnemyStateSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private static final float UP = 5.5f, DOWN = 5.5f;

    @Override public void addedToEngine(Engine eng){
        entities = eng.getEntitiesFor(Family.all(EnemyComponent.class).get());
    }

    @Override public void update(float dt){
        for(Entity e:entities){
            EnemyComponent ec = e.getComponent(EnemyComponent.class);

            /* freeze body once after death */
            if(ec.state==EnemyState.DEAD && ec.needsFreeze){
                ec.body.setLinearVelocity(0,0);
                ec.body.setGravityScale(0);
                ec.body.setType(BodyDef.BodyType.StaticBody);
                ec.needsFreeze=false;
            }
            if(ec.state==EnemyState.DEAD) continue;

            /* HURT countdown */
            if(ec.state==EnemyState.HURT){
                ec.hurtTimer-=dt;
                if(ec.hurtTimer<=0){
                    ec.state=EnemyState.IDLE;
                    ec.isHurt=false;
                    ec.animTime=0;
                }
                continue;
            }

            if(ec.attacking) continue;

            /* On‑ground vs air */
            if(ec.isGrounded()){
                ec.state = Math.abs(ec.body.getLinearVelocity().x) > 0.1f
                    ? EnemyState.CHASE : EnemyState.IDLE;
            }else{
                ec.state = ec.body.getLinearVelocity().y > 0
                    ? EnemyState.JUMP : EnemyState.AIRSPIN;
            }

            /* gravity scaling like player */
            ec.body.setGravityScale(
                ec.body.getLinearVelocity().y > 0 ? UP : DOWN);
        }
    }
}
