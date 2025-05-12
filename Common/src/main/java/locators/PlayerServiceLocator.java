package locators;

import services.player.IPlayerService;
import services.player.NoopPlayerService;

import java.util.ServiceLoader;

public final class PlayerServiceLocator {
    private static IPlayerService instance;

    private PlayerServiceLocator() {}

    public static IPlayerService get() {
        if (instance == null) {
            instance = ServiceLoader.load(IPlayerService.class).findFirst().orElse(new NoopPlayerService());
        }
        return instance;
    }
}

