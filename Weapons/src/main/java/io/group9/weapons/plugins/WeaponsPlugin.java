package io.group9.weapons.plugins;

import com.badlogic.ashley.core.Engine;
import io.group9.CoreResources;
import io.group9.weapons.ContactReceivers.WeaponPickupReceiver;
import io.group9.weapons.BodyDestroySystem;
import io.group9.weapons.WeaponSwitchSystem;
import plugins.ECSPlugin;
import services.IInventoryService;

import java.util.ServiceLoader;

public class WeaponsPlugin implements ECSPlugin {
    @Override
    public void registerSystems(Engine engine) {
        // game‐logic systems
        engine.addSystem(new WeaponSwitchSystem());
        engine.addSystem(new BodyDestroySystem());
    }

    @Override
    public void createEntities(Engine engine) {
        // Inventory service setup
        IInventoryService inv = ServiceLoader.load(IInventoryService.class).findFirst().orElseThrow(() -> new IllegalStateException("No IInventoryService found"));
        CoreResources.setInventoryService(inv);

        CoreResources.getContactDispatcher().addReceiver(new WeaponPickupReceiver());


        // Spawn one sword and register its entity
        WeaponFactory.spawnSword(200, 200);

    }

    @Override
    public int getPriority() {
        return 5;
    }
}

