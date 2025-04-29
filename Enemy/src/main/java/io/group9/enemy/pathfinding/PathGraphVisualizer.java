package io.group9.enemy.pathfinding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

public class PathGraphVisualizer {
    private PathGraph pathGraph;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private float unitScale;

    public PathGraphVisualizer(PathGraph pathGraph,
                               OrthographicCamera camera,
                               float unitScale) {
        this.pathGraph   = pathGraph;
        this.camera      = camera;
        this.unitScale   = unitScale;
        this.shapeRenderer = new ShapeRenderer();
    }

    private void begin(ShapeRenderer.ShapeType type) {
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(type);
    }

    public void renderConnections() {
        begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (PathNode n : pathGraph.getNodes()) {
            Vector2 p = new Vector2(n.x, n.y).scl(unitScale);
            for (PathNode neigh : n.neighbors) {
                Vector2 q = new Vector2(neigh.x, neigh.y).scl(unitScale);
                shapeRenderer.line(p, q);
            }
        }

        shapeRenderer.end();
    }

    public void renderNodes() {
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE); // Color for the nodes

        float radius = 5 * unitScale;  // adjust radius (in world units) as needed
        for (PathNode node : pathGraph.getNodes()) {
            // if node.x,node.y are in pixels, convert to meters:
            float x = node.x * unitScale;
            float y = node.y * unitScale;
            shapeRenderer.circle(x, y, radius);
        }

        shapeRenderer.end();
    }

    public void render() {
        renderConnections();
        renderNodes();
    }
}



