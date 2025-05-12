package io.group9.player.plugins;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;
import data.WorldProvider;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import locators.PlayerServiceLocator;
import services.player.IPlayerService;

public class PlayerFactory {

    public PlayerFactory() {}
    public static void spawn(Engine eng) {

        World world = WorldProvider.getWorld();

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(250f / CoreResources.PPM, 950f / CoreResources.PPM);

        Body body = world.createBody(bd);
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

        body.setUserData(pc);

        Entity playerE = new Entity();
        playerE.add(pc);
        eng.addEntity(playerE);

        pc.entity = playerE;

        IPlayerService svc = PlayerServiceLocator.get();
        svc.setPlayerBody(body);
        svc.setPlayerEntity(playerE);
        svc.setHealth(pc.health);
        svc.setMaxHealth(pc.maxHealth);
    }
}
