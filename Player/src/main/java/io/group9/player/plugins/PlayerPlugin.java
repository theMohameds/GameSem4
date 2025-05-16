package io.group9.player.plugins;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import contact.IContactDispatcherService;
import io.group9.player.contactReceivers.AttackContactReceiver;
import io.group9.player.contactReceivers.PlayerContactReceiver;
import io.group9.player.system.*;
import locators.ContactDispatcherLocator;
import plugins.ECSPlugin;

public class PlayerPlugin implements ECSPlugin {
    private boolean spawned = false;
    IContactDispatcherService dispatcher = ContactDispatcherLocator.get();

    @Override public void registerSystems(Engine eng) {
        eng.addSystem(new PlayerAnimationRenderer());
        eng.addSystem(new PlayerAttackSystem());
        eng.addSystem(new PlayerInputSystem());
        eng.addSystem(new PlayerStateSystem());

        dispatcher.addReceiver(new PlayerContactReceiver());
        dispatcher.addReceiver(new AttackContactReceiver());
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
