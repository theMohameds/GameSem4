package io.group9.enemy.pathfinding;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NodeGenerator {
    public static List<Vector2> generateStandingPoints(TiledMapTileLayer collisionLayer) {
        Set<Vector2> gridPoints = new LinkedHashSet<>();
        int w     = collisionLayer.getWidth();
        int h     = collisionLayer.getHeight();

        for (int y = 1; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TiledMapTileLayer.Cell curr  = collisionLayer.getCell(x, y);
                TiledMapTileLayer.Cell below = collisionLayer.getCell(x, y - 1);

                if (curr == null && below != null && below.getTile() != null) {
                    gridPoints.add(new Vector2(x,     y));
                    gridPoints.add(new Vector2(x + 1, y));
                }
            }
        }

        return new ArrayList<>(gridPoints);
    }

}
