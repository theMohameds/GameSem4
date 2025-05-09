package locators;


import services.player.IPlayerService;

import java.util.ServiceLoader;

public final class PlayerServiceLocator {
    private static final IPlayerService INSTANCE =
        ServiceLoader.load(IPlayerService.class).findFirst().orElseThrow(() -> new IllegalStateException("No IPlayerService implementation found"));

    private PlayerServiceLocator() { /* no-op */ }

    public static IPlayerService get() {
        return INSTANCE;
    }
}
