package io.group9.weapon;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
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
        Gdx.app.log("WeaponPlugin", "Registering systems...");

        engine.addSystem(new WeaponSystem());
        Gdx.app.log("WeaponPlugin", "WeaponSystem added to engine");

        engine.addSystem(new WeaponRendererSystem());
        Gdx.app.log("WeaponPlugin", "WeaponRendererSystem added to engine");

        World world = CoreResources.getWorld();
        contactReceiver = new WeaponContactReceiver(engine, world);
        CoreResources.getContactDispatcher().addReceiver(contactReceiver);
        Gdx.app.log("WeaponPlugin", "WeaponContactReceiver registered");

        engine.addSystem(new WeaponContactUpdateSystem(contactReceiver));
        Gdx.app.log("WeaponPlugin", "WeaponContactUpdateSystem added");
    }

    @Override
    public void createEntities(Engine engine) {

        Gdx.app.log("WeaponPlugin", "createEntities() called (no initial weapons)");
    }


    @Override
    public int getPriority() {
        return 3;
    }
}
