package io.group9;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import plugins.GameMapProvider;

public class CoreResources {
    private static World world;
    private static OrthographicCamera camera;
    private static Body playerBody;
    private static Entity playerEntity;
    private static CoreContactDispatcher contactDispatcher;

    // Pixels per meter conversion.
    public static final float PPM = 16f;

    public static void setWorld(World w) { world = w; }
    public static World getWorld() { return world; }

    public static void setCamera(OrthographicCamera cam) { camera = cam; }
    public static OrthographicCamera getCamera() { return camera; }

    public static void setPlayerBody(Body body) { playerBody = body; }
    public static Body getPlayerBody() { return playerBody; }

    public static void setPlayerEntity(Entity entity) { playerEntity = entity; }
    public static Entity getPlayerEntity() { return playerEntity; }

    public static void setContactDispatcher(CoreContactDispatcher dispatcher) { contactDispatcher = dispatcher; }
    public static CoreContactDispatcher getContactDispatcher() { return contactDispatcher; }

    private static GameMapProvider gameMapProvider;

    public static void setGameMapProvider(GameMapProvider prov) {
        gameMapProvider = prov;
    }

    public static GameMapProvider getGameMapProvider() {
        return gameMapProvider;
    }


}
