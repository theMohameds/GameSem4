package io.group9.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import io.group9.player.system.*;
import plugins.ECSPlugin;

public class PlayerPlugin implements ECSPlugin {
    @Override
    public void registerSystems(Engine engine) {
        engine.addSystem(new PlayerAnimationRenderer());
        engine.addSystem(new PlayerAttackSystem());
        engine.addSystem(new PlayerInputSystem());
        engine.addSystem(new PlayerStateSystem());

        CoreResources.getContactDispatcher().addReceiver(new PlayerContactReceiver());
        CoreResources.getContactDispatcher().addReceiver(new AttackContactReceiver());

    }

    @Override
    public void createEntities(Engine engine) {
        Gdx.app.log("PlayerPlugin", "Creating Player Entity");
        World world = CoreResources.getWorld();
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;

        bd.position.set(100 / CoreResources.PPM, 150 / CoreResources.PPM);
        bd.fixedRotation = true;
        Body body = world.createBody(bd);

        body.setLinearDamping(0f);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / CoreResources.PPM, 15 / CoreResources.PPM);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.5f;
        fd.friction = 0f;
        fd.restitution = 0f;
        body.createFixture(fd);
        shape.dispose();

        body.setUserData("player");

        Entity playerEntity = new Entity();
        PlayerComponent pc = new PlayerComponent();
        pc.body = body;
        pc.jumpsLeft = pc.maxJumps;
        playerEntity.add(pc);
        engine.addEntity(playerEntity);

        CoreResources.setPlayerEntity(playerEntity);
        CoreResources.setPlayerBody(body);
    }

    @Override
    public int getPriority() {
        return 2;
    }
}


