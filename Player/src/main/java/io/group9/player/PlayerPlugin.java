package io.group9.player;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import io.group9.player.system.*;
import plugins.ECSPlugin;

public class PlayerPlugin implements ECSPlugin {

    @Override public void registerSystems(Engine eng) {
        eng.addSystem(new PlayerAnimationRenderer());
        eng.addSystem(new PlayerAttackSystem());
        eng.addSystem(new PlayerInputSystem());
        eng.addSystem(new PlayerStateSystem());

        CoreResources.getContactDispatcher().addReceiver(new PlayerContactReceiver());
        CoreResources.getContactDispatcher().addReceiver(new AttackContactReceiver());
    }

    @Override public void createEntities(Engine eng) {
        Gdx.app.log("PlayerPlugin", "Creating Player Entity");
        World w = CoreResources.getWorld();

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(250f / CoreResources.PPM, 950f / CoreResources.PPM);
        Body body = w.createBody(bd);
        body.setSleepingAllowed(false);
        body.setLinearDamping(0f);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8f / CoreResources.PPM, 15f / CoreResources.PPM);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 0.5f;
        fd.friction = 0f;
        fd.filter.groupIndex = -1;
        body.createFixture(fd);
        shape.dispose();

        PlayerComponent pc = new PlayerComponent();
        pc.body      = body;
        pc.jumpsLeft = pc.maxJumps;
        pc.facingLeft = false;

        body.setUserData(pc);
        CoreResources.setPlayerBody(body);
        CoreResources.setPlayerHealth(pc.health);

        Entity playerE = new Entity();
        playerE.add(pc);
        eng.addEntity(playerE);
        CoreResources.setPlayerEntity(playerE);
    }

    @Override public int getPriority() { return 2; }
}
