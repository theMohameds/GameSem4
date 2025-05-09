package io.group9.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.group9.CoreResources;
import io.group9.player.components.PlayerComponent;
import io.group9.player.system.*;
import plugins.ECSPlugin;
import services.player.IPlayerService;
import locators.PlayerServiceLocator;

public class PlayerPlugin implements ECSPlugin {

    @Override public void registerSystems(Engine eng) {
        eng.addSystem(new PlayerAnimationRenderer());
        eng.addSystem(new PlayerAttackSystem());
        eng.addSystem(new PlayerInputSystem());
        eng.addSystem(new PlayerStateSystem());

        CoreResources.getContactDispatcher().addReceiver(new PlayerContactReceiver());
        CoreResources.getContactDispatcher().addReceiver(new AttackContactReceiver());
    }

    @Override
    public void createEntities(Engine eng) {
        Gdx.app.log("PlayerPlugin", "Creating Player Entity");

        // 1) Create the Box2D body
        World world = CoreResources.getWorld();
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

        // 2) Create & populate component
        PlayerComponent pc = new PlayerComponent();
        pc.body      = body;
        pc.jumpsLeft = pc.maxJumps;
        // set color, etc. if you have settings:
        // pc.color = YOUR_SETTINGS.getPlayerColor();

        // link body → component for contacts
        body.setUserData(pc);

        // 3) Make the Ashley entity
        Entity playerE = new Entity();
        playerE.add(pc);
        eng.addEntity(playerE);

        // now complete cross-refs
        pc.entity = playerE;

        // 4) initialize your SPI player service
        IPlayerService svc = PlayerServiceLocator.get();
        svc.setPlayerBody(body);
        svc.setPlayerEntity(playerE);
        svc.setHealth(pc.health);
        svc.setMaxHealth(pc.maxHealth);
        //CoreResources.setPlayerBody(body);
        //CoreResources.setPlayerEntity(playerE);
        //CoreResources.setPlayerHealth(pc.health);
    }

    @Override public int getPriority() { return 2; }
}
