package io.group9.weapons;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.group9.CoreResources;
import locators.InventoryServiceLocator;
import services.IInventoryService;
import services.IWeapon;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class WeaponSwitchSystem extends EntitySystem {

    @Override
    public void addedToEngine(Engine engine) {

    }

    @Override
    public void update(float dt) {
        IInventoryService inv = InventoryServiceLocator.getInventoryService();
        Entity player = CoreResources.getPlayerEntity();

        if (inv == null || player == null) {
            Gdx.app.log("WeaponSwitch","inv or player null → inv=" + inv + ", player=" + player);
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            List<IWeapon> list = inv.getInventory(player);
            Gdx.app.log("WeaponSwitch", "Pressed 1; inventory=" + list);

            if (list.isEmpty()) {
                Gdx.app.log("WeaponSwitch", "No weapon in slot 1");
                return;
            }

            IWeapon slot0 = list.get(0);
            Optional<IWeapon> cur = inv.getCurrentWeapon(player);
            Gdx.app.log("WeaponSwitch", "Currently equipped = " + cur);

            if (cur.filter(w -> w.equals(slot0)).isPresent()) {
                inv.selectSlot(player, -1);
                Gdx.app.log("WeaponSwitch", "→ Unequipped " + slot0.getName());
            } else {
                inv.selectSlot(player, 0);
                Gdx.app.log("WeaponSwitch", "→ Equipped " + slot0.getName());
            }
        }

    }
}
