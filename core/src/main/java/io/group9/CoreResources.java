package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.physics.box2d.World;

public class CoreResources {
    public static World getWorld() {
        return world;
    }

    public static void setWorld(World world) {
        CoreResources.world = world;
    }

    public static World world;

    public static Engine getEngine() {
        return engine;
    }

    public static void setEngine(Engine engine) {
        CoreResources.engine = engine;
    }

    public static Engine engine;
}
