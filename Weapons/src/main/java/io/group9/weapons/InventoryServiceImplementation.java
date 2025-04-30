package io.group9.weapons;

import services.IInventoryService;
import services.IWeapon;
import com.badlogic.ashley.core.Entity;
import java.util.*;

public class InventoryServiceImplementation implements IInventoryService {
    private final Map<Entity, List<IWeapon>> invs = new HashMap<>();
    private final Map<Entity, IWeapon> current = new HashMap<>();

    @Override
    public void add(Entity owner, IWeapon weapon) {
        invs.computeIfAbsent(owner, e -> new ArrayList<>()).add(weapon);
        // auto-equip
        //current.putIfAbsent(owner, weapon);
    }

    @Override
    public void remove(Entity owner, IWeapon weapon) {
        List<IWeapon> list = invs.get(owner);
        if (list != null) {
            list.remove(weapon);
            if (weapon.equals(current.get(owner))) {
                current.remove(owner);
            }
        }
    }

    @Override
    public void selectSlot(Entity owner, int slotIndex) {
        List<IWeapon> list = invs.get(owner);
        if (list != null && slotIndex >= 0 && slotIndex < list.size()) {
            current.put(owner, list.get(slotIndex));
        } else {
            current.remove(owner);
        }
    }

    @Override
    public Optional<IWeapon> getCurrentWeapon(Entity owner) {
        return Optional.ofNullable(current.get(owner));
    }

    @Override
    public List<IWeapon> getInventory(Entity owner) {
        return Collections.unmodifiableList(invs.getOrDefault(owner, Collections.emptyList()));
    }
}
