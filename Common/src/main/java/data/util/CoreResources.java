package data.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import data.GameMapProvider;

import java.util.ArrayList;
import java.util.List;

public class CoreResources {

    // Pixels per meter conversion.
    public static final float PPM = 16f;

    private static GameMapProvider gameMapProvider;
    public static void setGameMapProvider(GameMapProvider prov) {
        gameMapProvider = prov;
    }
    public static GameMapProvider getGameMapProvider() {
        return gameMapProvider;
    }

    private static volatile boolean roundFrozen = false;
    public static void setRoundFrozen(boolean f) { roundFrozen = f; }
    public static boolean isRoundFrozen() { return roundFrozen; }
    public static List<Vector2> getNodePositions() {
        return nodePositions;
    }
    public static void setNodePositions(List<Vector2> nodePositions) {
        CoreResources.nodePositions = nodePositions;
    }

    public static List<Vector2> nodePositions = new ArrayList<>();
    public static TiledMapTileLayer getCollisionLayer() {
        return collisionLayer;
    }
    public static void setCollisionLayer(TiledMapTileLayer collisionLayer) {
        CoreResources.collisionLayer = collisionLayer;
    }
    public static TiledMapTileLayer collisionLayer;
    public static ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    public static void setShapeRenderer(ShapeRenderer shapeRenderer) {
        CoreResources.shapeRenderer = shapeRenderer;
    }
    public static ShapeRenderer shapeRenderer;

}
