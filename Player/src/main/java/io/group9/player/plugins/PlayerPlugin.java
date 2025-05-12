package io.group9.player.plugins;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import data.WorldProvider;
import io.group9.CoreResources;
import io.group9.player.contactReceivers.AttackContactReceiver;
import io.group9.player.contactReceivers.PlayerContactReceiver;
import io.group9.player.components.PlayerComponent;
import io.group9.player.system.*;
import plugins.ECSPlugin;
import services.player.IPlayerService;
import locators.PlayerServiceLocator;

public class PlayerPlugin implements ECSPlugin {
    private boolean spawned = false;

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
        if (!spawned) {
            PlayerFactory.spawn(eng);
            spawned = true;
        }
    }

    @Override public int getPriority() { return 2; }
}
