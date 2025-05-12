
package locators;

import services.weapon.IInventoryService;
import services.weapon.NoopInventoryService;

import java.util.ServiceLoader;

public final class InventoryServiceLocator {
    private static IInventoryService instance;
    private InventoryServiceLocator() {}

    public static IInventoryService getInventoryService() {
        if (instance == null) {
            instance = ServiceLoader.load(IInventoryService.class).findFirst().orElse(new NoopInventoryService());
        }
        return instance;
    }
}
