package io.group9.weapons.plugins;

import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.weapons.ContactReceivers.WeaponPickupReceiver;
import io.group9.weapons.BodyDestroySystem;
import io.group9.weapons.WeaponSwitchSystem;
import io.group9.weapons.systems.WeaponRenderSystem;
import plugins.ECSPlugin;
import services.IInventoryService;

import java.util.ServiceLoader;

public class WeaponsPlugin implements ECSPlugin {
    @Override
    public void registerSystems(Engine engine) {
        engine.addSystem(new WeaponSwitchSystem());
        engine.addSystem(new BodyDestroySystem());
        engine.addSystem(new WeaponRenderSystem());
    }

    @Override
    public void createEntities(Engine engine) {
        CoreResources.getContactDispatcher().addReceiver(new WeaponPickupReceiver());

        WeaponFactory.spawnSword(160, 200);
    }

    @Override
    public int getPriority() {
        return 5;
    }
}

