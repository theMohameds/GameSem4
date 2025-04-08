package io.group9.gamemap.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import io.group9.CoreResources;


import java.util.*;

public class GameMapSystem extends EntitySystem {

    // The libGDX TiledMap and renderer
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    // The specific layer we use for collisions
    private TiledMapTileLayer collisionLayer;

    // Storage for intermediate tile rectangles (before merging)
    private final List<Rectangle> rawRectangles = new ArrayList<>();
    // Storage for merged rectangles
    private final List<Rectangle> mergedRectangles = new ArrayList<>();

    // Box2D world reference (passed in from outside)
    private World world;

    // To draw the map
    private OrthographicCamera camera;

    // Box2D bodies for collisions
    private final List<Body> collisionBodies = new ArrayList<>();


    public GameMapSystem(World world, String mapPath, int collisionLayerIndex, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;

        tiledMap = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get(collisionLayerIndex);
        gatherCollisionTiles();
        mergeRectangles();
        createCollisionBodies();
    }
    @Override
    public void update(float deltaTime) {
        camera.update();
        render();
    }
    private void gatherCollisionTiles() {
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();
        int tileWidth = (int) collisionLayer.getTileWidth();
        int tileHeight = (int) collisionLayer.getTileHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = collisionLayer.getCell(x, y);
                if (cell != null) {
                    TiledMapTile tile = cell.getTile();
                    if (tile != null) {
                        // Store each tile as a rectangle in pixel/world coords
                        float rx = x * tileWidth;
                        float ry = y * tileHeight;
                        Rectangle rect = new Rectangle(rx, ry, tileWidth, tileHeight);
                        rawRectangles.add(rect);
                    }
                }
            }
        }
    }
    private void mergeRectangles() {
        // Step 1: Group by row and merge horizontally
        Map<Float, List<Rectangle>> rows = new HashMap<>();
        for (Rectangle rect : rawRectangles) {
            float y = rect.y;
            rows.computeIfAbsent(y, k -> new ArrayList<>()).add(rect);
        }

        List<Rectangle> horizontallyMerged = new ArrayList<>();

        // Merge horizontally for each row
        for (Map.Entry<Float, List<Rectangle>> entry : rows.entrySet()) {
            List<Rectangle> rowRects = entry.getValue();
            // Sort by x-coordinate
            rowRects.sort(Comparator.comparingDouble(r -> r.x));

            Rectangle current = new Rectangle(rowRects.get(0));
            for (int i = 1; i < rowRects.size(); i++) {
                Rectangle next = rowRects.get(i);
                // If next rect is exactly flush with current on the right side...
                if (current.x + current.width == next.x && current.y == next.y && current.height == next.height) {
                    // Extend the current rectangle
                    current.width += next.width;
                } else {
                    // Store the completed rectangle
                    horizontallyMerged.add(new Rectangle(current));
                    // Start a new merge
                    current.set(next);
                }
            }
            // Don't forget the last in the row
            horizontallyMerged.add(new Rectangle(current));
        }

        // Step 2: Merge vertically where possible
        // Sort by y first, then x
        horizontallyMerged.sort((a, b) -> {
            if (a.y != b.y) return Float.compare(a.y, b.y);
            return Float.compare(a.x, b.x);
        });

        boolean[] used = new boolean[horizontallyMerged.size()];
        for (int i = 0; i < horizontallyMerged.size(); i++) {
            if (used[i]) continue;
            Rectangle base = horizontallyMerged.get(i);

            for (int j = i + 1; j < horizontallyMerged.size(); j++) {
                if (used[j]) continue;
                Rectangle check = horizontallyMerged.get(j);

                // same x, same width, directly on top?
                if (check.x == base.x && check.width == base.width && check.y == base.y + base.height) {
                    base.height += check.height;
                    used[j] = true;
                }
            }
            // Add the expanded rectangle to final list
            mergedRectangles.add(new Rectangle(base));
        }

        // Not strictly necessary, but let's clear rawRectangles to avoid confusion
        rawRectangles.clear();
    }
    private void createCollisionBodies() {
        for (Rectangle rect : mergedRectangles) {
            // Box2D Body definition
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            // The body will be placed at the center of this rectangle
            bodyDef.position.set(
                rect.x + rect.width / 2f,
                rect.y + rect.height / 2f
            );

            // Create the body in the world
            Body body = world.createBody(bodyDef);

            // Create the shape for collision (Box2D boxes take half-width/half-height)
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(rect.width / 2f, rect.height / 2f);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 0f;    // Static bodies do not need density
            fixtureDef.friction = 0.2f; // Example friction
            fixtureDef.restitution = 0f; // Example restitution (bounciness)

            body.createFixture(fixtureDef);
            shape.dispose();

            collisionBodies.add(body);
        }
    }
    public void render() {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }
    public void dispose() {
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        // Note: do NOT dispose the Box2D World here if you're using it elsewhere.
        // Also be cautious about destroying bodies if you might need them later.
    }

}
