package io.group9.weapons.plugins;

import com.badlogic.ashley.core.Engine;
import contact.IContactDispatcherService;
import io.group9.weapons.ContactReceivers.WeaponPickupReceiver;
import io.group9.weapons.systems.BodyDestroySystem;
import io.group9.weapons.systems.WeaponSwitchSystem;
import io.group9.weapons.systems.WeaponRenderSystem;
import locators.ContactDispatcherLocator;
import plugins.ECSPlugin;

public class WeaponsPlugin implements ECSPlugin {
    IContactDispatcherService dispatcher = ContactDispatcherLocator.get();
    @Override
    public void registerSystems(Engine engine) {
        engine.addSystem(new WeaponSwitchSystem());
        engine.addSystem(new BodyDestroySystem());
        engine.addSystem(new WeaponRenderSystem());
    }

    @Override
    public void createEntities(Engine engine) {
        dispatcher.addReceiver(new WeaponPickupReceiver());

        WeaponFactory.spawnSword(160, 200);
    }

    @Override
    public int getPriority() {
        return 5;
    }
}

