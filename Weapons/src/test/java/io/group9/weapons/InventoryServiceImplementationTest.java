package io.group9.weapons;

import com.badlogic.ashley.core.Entity;
import io.group9.weapons.components.SwordComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.weapon.IWeapon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceImplementationTest {

    private InventoryServiceImplementation inventory;
    private Entity owner;
    private IWeapon sword;

    @BeforeEach
    void setUp() {
        inventory = new InventoryServiceImplementation();
        owner = new Entity();
        sword = new SwordComponent();
    }

    @Test
    void addAndGetInventory() {
        assertTrue(inventory.getInventory(owner).isEmpty(), "Inventory should start empty");
        inventory.add(owner, sword);

        List<IWeapon> inv = inventory.getInventory(owner);
        assertEquals(1, inv.size(), "Inventory size should be 1 after add");
        assertSame(sword, inv.get(0), "Stored weapon should be the same instance");
    }

    @Test
    void removeWeapon() {
        inventory.add(owner, sword);
        inventory.remove(owner, sword);
        assertTrue(inventory.getInventory(owner).isEmpty(), "Inventory should be empty after removal");
    }

    @Test
    void selectSlotAndGetCurrentWeapon() {
        inventory.add(owner, sword);
        inventory.selectSlot(owner, 0);

        assertTrue(inventory.getCurrentWeapon(owner).isPresent(), "Current weapon should be present");
        assertSame(sword, inventory.getCurrentWeapon(owner).get(), "Current weapon should be the sword");
    }

    @Test
    void selectInvalidSlotClearsCurrentWeapon() {
        inventory.add(owner, sword);
        inventory.selectSlot(owner, 5);

        assertFalse(inventory.getCurrentWeapon(owner).isPresent(), "Selecting invalid slot clears current weapon");
    }

    @Test
    void inventoryListIsUnmodifiable() {
        inventory.add(owner, sword);
        assertThrows(UnsupportedOperationException.class,
            () -> inventory.getInventory(owner).add(new SwordComponent()),
            "Inventory list should be unmodifiable");
    }
}
