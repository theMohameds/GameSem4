package io.group9.enemy.pathfinding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class PathGraphVisualizer {
    private final PathGraph pathGraph;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final float unitScale;

    // === Color Definitions ===
    // For path lines
    private static final Color RUN_COLOR = new Color(70 / 255f, 130 / 255f, 180 / 255f, 1f);       // Steel Blue
    private static final Color JUMP_COLOR = new Color(255 / 255f, 191 / 255f, 0 / 255f, 1f);        // Amber Yellow
    private static final Color DOUBLE_JUMP_COLOR = new Color(220 / 255f, 20 / 255f, 60 / 255f, 1f); // Crimson Red

    // For node circle fills
    private static final Color NODE_PATH_COLOR = new Color(72 / 255f, 61 / 255f, 139 / 255f, 1f);   // Dark Slate Blue

    // For node types
    private static final Color NORMAL_NODE_COLOR = new Color(65 / 255f, 105 / 255f, 225 / 255f, 1f);      // Royal Blue
    private static final Color JUMP_NODE_COLOR = new Color(255 / 255f, 165 / 255f, 0 / 255f, 1f);         // Orange
    private static final Color DOUBLE_JUMP_NODE_COLOR = new Color(186 / 255f, 85 / 255f, 211 / 255f, 1f); // Medium Orchid
    private static final Color ALL_JUMP_NODE_COLOR = new Color(0 / 255f, 255 / 255f, 127 / 255f, 1f);     // Spring Green

    public PathGraphVisualizer(PathGraph pathGraph, OrthographicCamera camera, float unitScale) {
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
        shapeRenderer.setColor(RUN_COLOR); // optional: show walkable graph

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
        begin(ShapeRenderer.ShapeType.Filled);
        float radius = 5 * unitScale;

        for (PathNode node : pathGraph.getNodes()) {
            float x = node.x * unitScale;
            float y = node.y * unitScale;

            // Set color based on movement requirements
            if (node.requiresJump && node.requiresDoubleJump) {
                shapeRenderer.setColor(ALL_JUMP_NODE_COLOR);
            } else if (node.requiresDoubleJump) {
                shapeRenderer.setColor(DOUBLE_JUMP_NODE_COLOR);
            } else if (node.requiresJump) {
                shapeRenderer.setColor(JUMP_NODE_COLOR);
            } else {
                shapeRenderer.setColor(NORMAL_NODE_COLOR);
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

            // Set color by movement type
            if (next.requiresDoubleJump) {
                shapeRenderer.setColor(DOUBLE_JUMP_COLOR);
            } else if (next.requiresJump) {
                shapeRenderer.setColor(JUMP_COLOR);
            } else {
                shapeRenderer.setColor(RUN_COLOR);
            }

            shapeRenderer.rectLine(from, to, thickness);
        }

        // Draw path nodes
        float radius = 6 * unitScale;
        for (PathNode node : path) {
            float x = node.x * unitScale;
            float y = node.y * unitScale;
            shapeRenderer.setColor(NODE_PATH_COLOR);
            shapeRenderer.circle(x, y, radius);
        }

        shapeRenderer.end();
    }

    public void render() {
        renderConnections();
        renderNodes();
    }
}
