package locators;

import services.enemy.IEnemyService;
import services.enemy.NoopEnemyService;

import java.util.ServiceLoader;

public final class EnemyServiceLocator {
    private static IEnemyService instance;
    private EnemyServiceLocator(){}
    public static IEnemyService get() {
        if (instance == null) {
            instance = ServiceLoader.load(IEnemyService.class).findFirst().orElse(new NoopEnemyService());
        }
        return instance;
    }
}
