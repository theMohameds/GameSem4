package data;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Vector2;

public final class WorldProvider {
    private static final World INSTANCE =
        new World(new Vector2(0, -9.81f), true);

    private WorldProvider() { /* no instances */ }

    public static World getWorld() {
        return INSTANCE;
    }
}

