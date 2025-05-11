package io.group9.weapons.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import locators.InventoryServiceLocator;
import locators.PlayerServiceLocator;
import services.weapon.IInventoryService;
import services.weapon.IWeapon;
import services.player.IPlayerService;

import java.util.List;
import java.util.Optional;

public class WeaponSwitchSystem extends EntitySystem {

    @Override
    public void addedToEngine(Engine engine) {

    }

    @Override
    public void update(float dt) {
        IInventoryService inv = InventoryServiceLocator.getInventoryService();
        IPlayerService playerSvc = PlayerServiceLocator.get();
        Entity player = playerSvc.getPlayerEntity();

        if (inv == null || player == null) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            List<IWeapon> list = inv.getInventory(player);

            if (list.isEmpty()) {
                return;
            }

            IWeapon slot0 = list.get(0);
            Optional<IWeapon> cur = inv.getCurrentWeapon(player);

            if (cur.filter(w -> w.equals(slot0)).isPresent()) {
                inv.selectSlot(player, -1);
            } else {
                inv.selectSlot(player, 0);
            }
        }

    }
}
