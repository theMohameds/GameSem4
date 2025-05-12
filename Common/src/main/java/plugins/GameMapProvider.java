package plugins;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;

/**
 * A simple interface for map collision data.
 */
public interface GameMapProvider {
    /** @return merged collision rects in world units (meters) */
    List<Rectangle> getMergedWorldRectangles();

    /** @return number of cells horizontally in the collision layer */
    int getLayerWidth();

    /** @return number of cells vertically in the collision layer */
    int getLayerHeight();

    /** @return size of one tile cell in Box2D world units (meters) */
    float getCellSizeMeters();
}

