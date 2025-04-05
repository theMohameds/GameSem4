package plugins;

import com.badlogic.ashley.core.Engine;

public interface ECSPlugin {

    void registerSystems(Engine engine);

    void createEntities(Engine engine);

    int getPriority(); // Lower values mean higher priority (loaded first)

}
