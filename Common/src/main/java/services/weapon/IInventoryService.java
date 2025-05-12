package services.weapon;

import com.badlogic.ashley.core.Entity;
import java.util.List;
import java.util.Optional;


public interface IInventoryService {
    void add(Entity owner, IWeapon weapon);

    void remove(Entity owner, IWeapon weapon);

    void selectSlot(Entity owner, int slotIndex);

    Optional<IWeapon> getCurrentWeapon(Entity owner);

    List<IWeapon> getInventory(Entity owner);
}
