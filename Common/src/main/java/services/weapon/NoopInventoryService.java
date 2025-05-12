package services.weapon;

import com.badlogic.ashley.core.Entity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NoopInventoryService implements IInventoryService {
    @Override
    public void add(Entity e, IWeapon w) { }
    @Override
    public void remove(Entity owner, IWeapon weapon) { }
    @Override
    public List<IWeapon> getInventory(Entity e) { return Collections.emptyList(); }
    @Override
    public Optional<IWeapon> getCurrentWeapon(Entity e) { return Optional.empty(); }
    @Override
    public void selectSlot(Entity e, int slot)  { }
}
