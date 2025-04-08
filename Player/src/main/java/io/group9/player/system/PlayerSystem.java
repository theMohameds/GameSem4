package io.group9.player.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import io.group9.player.component.PlayerComponent;
import com.badlogic.gdx.physics.box2d.Body;

public class PlayerSystem extends EntitySystem {
    private static final float MAX_SPEED = 300.0f;
    private static final float ACCELERATION = 900.0f;
    private static final float JUMP_IMPULSE = 500.0f;

    private ImmutableArray<Entity> entities;
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            PlayerComponent player = pm.get(entity);
            Body body = player.body;

            if (body == null) continue;

            Vector2 velocity = body.getLinearVelocity();
            int horizontalInput = 0;
            System.out.println(velocity);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) horizontalInput -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) horizontalInput += 1;

            if (horizontalInput != 0) {
                float mass = body.getMass();
                body.applyForceToCenter(new Vector2(horizontalInput * mass * ACCELERATION, 0), true);
            } else if (player.onGround) {
                body.setLinearVelocity(0, velocity.y); // Stop horizontal movement
            }

            if (Math.abs(velocity.x) > MAX_SPEED) {
                body.setLinearVelocity(Math.signum(velocity.x) * MAX_SPEED, velocity.y);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && player.jumpCount < 2) {
                body.setLinearVelocity(velocity.x, 0);
                body.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), body.getWorldCenter(), true);
                player.jumpCount++;
                player.onGround = false;
            }
        }
    }
}


