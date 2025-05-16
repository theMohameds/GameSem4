
package io.group9.gamemap.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import util.CoreResources;


import java.util.*;

public class GameMapSystem extends EntitySystem {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer collisionLayer;
    private final List<Rectangle> rawRectangles    = new ArrayList<>();
    private final List<Rectangle> mergedRectangles = new ArrayList<>();
    private World world;
    private OrthographicCamera camera;
    private final List<Body> collisionBodies = new ArrayList<>(); // Don't think we need it

    // Conversion factor from pixels to meters.
    private static final float UNIT_SCALE = 1 / CoreResources.PPM;

    // Grid info for A* nav‑graph
    private final int layerWidth;
    private final int layerHeight;
    private final float cellSizeMeters;
    public ArrayList<Vector2> nodePositions = new ArrayList<>();

    public GameMapSystem(World world,
                         String mapPath,
                         OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;

        // Load the TiledMap and renderer
        tiledMap = new TmxMapLoader().load(mapPath);
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        // Find the layer named "Ground"
        collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Ground");
        if (collisionLayer == null) {
            throw new IllegalArgumentException("Tiled map is missing a layer named 'Ground'");
        }
        CoreResources.setCollisionLayer(collisionLayer);

        this.layerWidth = collisionLayer.getWidth();
        this.layerHeight = collisionLayer.getHeight();
        this.cellSizeMeters = collisionLayer.getTileWidth() * UNIT_SCALE;

        // Build collision rectangles and Box2D bodies
        gatherCollisionTiles();
        generateNodeEdges();
        mergeRectangles();
        createCollisionBodies();

        CoreResources.setNodePositions(nodePositions);
    }

    @Override
    public void update(float deltaTime) {
        if (camera == null) return;
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    private void gatherCollisionTiles() {
        int layerW = collisionLayer.getWidth();
        int layerH = collisionLayer.getHeight();
        int tileW  = collisionLayer.getTileWidth();
        int tileH  = collisionLayer.getTileHeight();

        for (int y = 0; y < layerH; y++) {
            for (int x = 0; x < layerW; x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    rawRectangles.add(new Rectangle(
                        x * tileW, y * tileH, tileW, tileH
                    ));
                }
            }
        }
    }

    private void mergeRectangles() {
        // Horizontal merge
        Map<Float, List<Rectangle>> rows = new HashMap<>();
        for (Rectangle r : rawRectangles) {
            rows.computeIfAbsent(r.y, k -> new ArrayList<>()).add(r);
        }
        List<Rectangle> horiz = new ArrayList<>();
        for (List<Rectangle> row : rows.values()) {
            row.sort(Comparator.comparingDouble(r -> r.x));
            Rectangle cur = new Rectangle(row.get(0));
            for (int i = 1; i < row.size(); i++) {
                Rectangle next = row.get(i);
                if (Math.abs(cur.x + cur.width - next.x) < 0.1f
                    && cur.y == next.y
                    && cur.height == next.height) {
                    cur.width += next.width;
                } else {
                    horiz.add(new Rectangle(cur));
                    cur.set(next);
                }
            }
            horiz.add(new Rectangle(cur));
        }
        // Vertical merge
        horiz.sort((a, b) -> {
            int cmp = Float.compare(a.y, b.y);
            return cmp != 0 ? cmp : Float.compare(a.x, b.x);
        });
        boolean[] used = new boolean[horiz.size()];
        for (int i = 0; i < horiz.size(); i++) {
            if (used[i]) continue;
            Rectangle base = new Rectangle(horiz.get(i));
            for (int j = i + 1; j < horiz.size(); j++) {
                if (used[j]) continue;
                Rectangle chk = horiz.get(j);
                if (Math.abs(chk.x - base.x) < 0.1f
                    && Math.abs(chk.width - base.width) < 0.1f
                    && Math.abs(chk.y - (base.y + base.height)) < 0.1f) {
                    base.height += chk.height;
                    used[j] = true;
                }
            }
            mergedRectangles.add(new Rectangle(base));
        }
        rawRectangles.clear();
    }

    private void createCollisionBodies() {
        for (Rectangle r : mergedRectangles) {
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.StaticBody;
            float worldX = (r.x + r.width / 2f) * UNIT_SCALE;
            float worldY = (r.y + r.height / 2f) * UNIT_SCALE;
            bd.position.set(worldX, worldY);

            Body body = world.createBody(bd);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox((r.width / 2f)  * UNIT_SCALE,
                (r.height / 2f) * UNIT_SCALE);

            FixtureDef fd = new FixtureDef();
            fd.shape      = shape;
            fd.friction   = 0.2f;
            fd.restitution= 0f;
            body.createFixture(fd);
            shape.dispose();

            body.setUserData("ground");
            collisionBodies.add(body);
        }
    }

    public void generateNodeEdges() {
        int layerW = collisionLayer.getWidth();
        int layerH = collisionLayer.getHeight();
        int tileW  = collisionLayer.getTileWidth();
        int tileH  = collisionLayer.getTileHeight();

        Set<Vector2> nodeSet = new HashSet<>();

        for (int y = 1; y < layerH; y++) {
            for (int x = 0; x < layerW; x++) {
                TiledMapTileLayer.Cell current = collisionLayer.getCell(x, y);
                TiledMapTileLayer.Cell below   = collisionLayer.getCell(x, y - 1);

                if (current == null && below != null && below.getTile() != null) {
                    float posY = y * tileH;
                    float leftX = x * tileW;
                    float rightX = (x + 1) * tileW;

                    nodeSet.add(new Vector2(leftX, posY));
                    nodeSet.add(new Vector2(rightX, posY));
                }
            }
        }

        nodePositions = new ArrayList<>(nodeSet);
    }
    public List<Rectangle> getMergedWorldRectangles() {
        List<Rectangle> out = new ArrayList<>();
        for (Rectangle r : mergedRectangles) {
            out.add(new Rectangle(
                r.x * UNIT_SCALE,
                r.y * UNIT_SCALE,
                r.width  * UNIT_SCALE,
                r.height * UNIT_SCALE
            ));
        }
        return out;
    }

    public void dispose() {
        if (tiledMap   != null) tiledMap.dispose();
        if (mapRenderer!= null) mapRenderer.dispose();
    }

    public int getLayerWidth() {
        return layerWidth;
    }

    public int getLayerHeight() {
        return layerHeight;
    }

    public float getCellSizeMeters() {
        return cellSizeMeters;
    }


}
