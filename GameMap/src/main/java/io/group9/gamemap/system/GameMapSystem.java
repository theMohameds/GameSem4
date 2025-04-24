package io.group9.gamemap.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.group9.CoreResources;

public class GameMapSystem extends EntitySystem {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer collisionLayer;
    private final List<Rectangle> rawRectangles = new ArrayList<>();
    private final List<Rectangle> mergedRectangles = new ArrayList<>();
    private World world;
    private OrthographicCamera camera;
    private final List<Body> collisionBodies = new ArrayList<>();

    // Conversion factor.
    private static final float UNIT_SCALE = 1 / CoreResources.PPM;

    public GameMapSystem(World world, String mapPath, int collisionLayerIndex, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;
        tiledMap = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);
        collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get(collisionLayerIndex);
        gatherCollisionTiles();
        mergeRectangles();
        createCollisionBodies();
    }

    @Override
    public void update(float deltaTime) {
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    private void gatherCollisionTiles() {
        int layerWidth = collisionLayer.getWidth();
        int layerHeight = collisionLayer.getHeight();
        int tileWidth = (int) collisionLayer.getTileWidth();
        int tileHeight = (int) collisionLayer.getTileHeight();

        for (int y = 0; y < layerHeight; y++) {
            for (int x = 0; x < layerWidth; x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    Rectangle rect = new Rectangle(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
                    rawRectangles.add(rect);
                }
            }
        }
    }

    private void mergeRectangles() {
        // Merge horizontally.
        Map<Float, List<Rectangle>> rows = new HashMap<>();
        for (Rectangle rect : rawRectangles) {
            rows.computeIfAbsent(rect.y, k -> new ArrayList<>()).add(rect);
        }
        List<Rectangle> horizMerged = new ArrayList<>();
        for (List<Rectangle> row : rows.values()) {
            row.sort(Comparator.comparingDouble(r -> r.x));
            Rectangle current = new Rectangle(row.get(0));
            for (int i = 1; i < row.size(); i++) {
                Rectangle next = row.get(i);
                if (Math.abs(current.x + current.width - next.x) < 0.1f
                    && current.y == next.y && current.height == next.height) {
                    current.width += next.width;
                } else {
                    horizMerged.add(new Rectangle(current));
                    current.set(next);
                }
            }
            horizMerged.add(new Rectangle(current));
        }
        // Merge vertically.
        horizMerged.sort((a, b) -> {
            int cmp = Float.compare(a.y, b.y);
            return cmp != 0 ? cmp : Float.compare(a.x, b.x);
        });
        boolean[] used = new boolean[horizMerged.size()];
        for (int i = 0; i < horizMerged.size(); i++) {
            if (used[i]) continue;
            Rectangle base = new Rectangle(horizMerged.get(i));
            for (int j = i + 1; j < horizMerged.size(); j++) {
                if (used[j]) continue;
                Rectangle check = horizMerged.get(j);
                if (Math.abs(check.x - base.x) < 0.1f && Math.abs(check.width - base.width) < 0.1f
                    && Math.abs(check.y - (base.y + base.height)) < 0.1f) {
                    base.height += check.height;
                    used[j] = true;
                }
            }
            mergedRectangles.add(new Rectangle(base));
        }
        rawRectangles.clear();
    }

    private void createCollisionBodies() {
        for (Rectangle rect : mergedRectangles) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            float worldX = (rect.x + rect.width / 2f) * UNIT_SCALE;
            float worldY = (rect.y + rect.height / 2f) * UNIT_SCALE;
            bodyDef.position.set(worldX, worldY);
            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox((rect.width / 2f) * UNIT_SCALE, (rect.height / 2f) * UNIT_SCALE);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.friction = 0.2f;
            fixtureDef.restitution = 0f;
            body.createFixture(fixtureDef);
            shape.dispose();

            // Tag the body as ground.
            body.setUserData("ground");
            collisionBodies.add(body);
        }
    }

    public void dispose() {
        if (tiledMap != null)
            tiledMap.dispose();
        if (mapRenderer != null)
            mapRenderer.dispose();
    }
}
