package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import plugins.GameMapProvider;
import services.IInventoryService;

public class CoreResources {
    private static World world;
    private static OrthographicCamera camera;
    private static Body playerBody;
    private static Entity playerEntity;
    private static CoreContactDispatcher contactDispatcher;
    private static Entity enemyEntity;

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

    private static int playerHealth;
    private static int enemyHealth;

    public static void setPlayerHealth(int hp) { playerHealth = hp; }
    public static int getPlayerHealth() { return playerHealth; }

    public static void setEnemyHealth(int hp) { enemyHealth  = hp; }
    public static int getEnemyHealth() { return enemyHealth; }

    private static Body enemyBody;

    public static void setEnemyBody(Body body)       { enemyBody = body; }
    public static Body getEnemyBody()                { return enemyBody; }
    private static volatile boolean roundFrozen = true;
    public static void   setRoundFrozen(boolean f) { roundFrozen = f; }
    public static boolean isRoundFrozen() { return roundFrozen; }

    public static void setEnemyEntity(Entity entity) { enemyEntity = entity; }
    public static Entity getEnemyEntity() { return enemyEntity; }

    private static IInventoryService inventoryService;
    public static void setInventoryService(IInventoryService svc) {
        inventoryService = svc;
    }
    public static IInventoryService getInventoryService() {
        return inventoryService;
    }

}
