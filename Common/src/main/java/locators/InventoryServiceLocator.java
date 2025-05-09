package locators;

import services.IInventoryService;

import java.util.ServiceLoader;

public final class InventoryServiceLocator {
    private static final IInventoryService INSTANCE =
        ServiceLoader.load(IInventoryService.class).findFirst().orElseThrow(() -> new IllegalStateException("No IInventoryService found"));

    private InventoryServiceLocator() { }

    public static IInventoryService getInventoryService() {
        return INSTANCE;
    }
}
