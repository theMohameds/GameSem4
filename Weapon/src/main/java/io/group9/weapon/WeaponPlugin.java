package io.group9.weapon;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.physics.box2d.World;
import io.group9.CoreResources;
import io.group9.weapon.system.WeaponContactUpdateSystem;
import plugins.ECSPlugin;
import io.group9.weapon.system.WeaponRendererSystem;
import io.group9.weapon.system.WeaponSystem;

public class WeaponPlugin implements ECSPlugin {
    private WeaponContactReceiver contactReceiver;

    @Override
    public void registerSystems(Engine engine) {
        engine.addSystem(new WeaponSystem());
        engine.addSystem(new WeaponRendererSystem());

        World world = CoreResources.getWorld();
        contactReceiver = new WeaponContactReceiver(engine, world);
        CoreResources.getContactDispatcher().addReceiver(contactReceiver);

        // Add system to process pending actions
        engine.addSystem(new WeaponContactUpdateSystem(contactReceiver));
    }

    @Override
    public void createEntities(Engine engine) {}

    @Override
    public int getPriority() {
        return 3;
    }
}
