package io.group9.player.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import services.player.IPlayerService;
import data.util.CoreResources;

public class PlayerServiceImplementation implements IPlayerService {
    private Entity playerEntity;
    private Body playerBody;
    private int health, maxHealth;
    private final ComponentMapper<PlayerComponent> mapper = ComponentMapper.getFor(PlayerComponent.class);

    @Override public int getHealth() { return health; }
    @Override public void setHealth(int h) { health = h;}
    @Override public int getMaxHealth() { return maxHealth; }
    @Override public void setMaxHealth(int mh) { maxHealth = mh; }
    @Override public Entity getPlayerEntity() { return playerEntity; }
    @Override public void setPlayerEntity(Entity e) { playerEntity = e; }
    @Override public Body getPlayerBody() { return playerBody; }
    @Override public void setPlayerBody(Body b) { playerBody = b; }
    @Override
    public void resetForRound(Vector2 spawnPoint) {
        setHealth(100);
        PlayerComponent c = mapper.get(playerEntity);

        c.health = c.maxHealth;
        c.jumpsLeft = c.maxJumps;
        c.attacking = false;
        c.attackRequested= false;
        c.isHurt = false;
        c.hurtTimer = 0f;
        c.attackTimer = 0f;
        c.blockTimer = 0f;
        c.wallHanging = false;
        c.wallHangingTimer = 0f;
        c.wallHangCooldownTimer = 0f;
        c.needsFreeze = false;
        c.state = PlayerComponent.State.IDLE;
        c.facingLeft = false;


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
        PlayerComponent c = mapper.get(playerEntity);
        c.needsFreeze = true;
    }
}

