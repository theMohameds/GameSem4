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
        this.pathGraph = pathGraph;
        this.camera = camera;
        this.unitScale = unitScale;
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

        float radius = 5 * unitScale;  // adjust radius (in world units) as needed
        for (PathNode node : pathGraph.getNodes()) {
            // if node.x,node.y are in pixels, convert to meters:
            float x = node.x * unitScale;
            float y = node.y * unitScale;

            // Set color based on jump or double jump requirement
            if (node.requiresDoubleJump && node.requiresJump){
                shapeRenderer.setColor(Color.GREEN);
            }else if (node.requiresDoubleJump) {
                shapeRenderer.setColor(Color.PURPLE);  // Color for double jump nodes
            } else if (node.requiresJump) {
                shapeRenderer.setColor(Color.ORANGE);  // Color for jump nodes
            } else {
                shapeRenderer.setColor(Color.BLUE);  // Default color for regular nodes
            }

            shapeRenderer.circle(x, y, radius);
        }

        shapeRenderer.end();
    }
    public void renderPath(List<PathNode> path) {
        if (path == null || path.size() < 2) return;

        begin(ShapeRenderer.ShapeType.Filled);
        float thickness = 4 * unitScale;

        for (int i = 0; i < path.size() - 1; i++) {
            PathNode curr = path.get(i);
            PathNode next = path.get(i + 1);

            Vector2 from = new Vector2(curr.x, curr.y).scl(unitScale);
            Vector2 to = new Vector2(next.x, next.y).scl(unitScale);

            // Determine movement type
            if (next.requiresDoubleJump) {
                shapeRenderer.setColor(Color.PURPLE);
            } else if (next.requiresJump) {
                shapeRenderer.setColor(Color.ORANGE);
            } else {
                shapeRenderer.setColor(Color.BLUE);
            }

            shapeRenderer.rectLine(from, to, thickness);
        }

        // Draw nodes as filled circles
        float radius = 6 * unitScale;
        for (PathNode node : path) {
            float x = node.x * unitScale;
            float y = node.y * unitScale;
            shapeRenderer.setColor(Color.FIREBRICK);  // Optionally vary per node
            shapeRenderer.circle(x, y, radius);
        }

        shapeRenderer.end();
    }


    public void render() {
        renderConnections();
        renderNodes();
    }
}
