package io.group9.enemy.pathfinding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

public class PathGraphVisualizer {
    private PathGraph pathGraph;
    private ShapeRenderer shapeRenderer;

    public PathGraphVisualizer(PathGraph pathGraph) {
        this.pathGraph = pathGraph;
        this.shapeRenderer = new ShapeRenderer();
    }

    // Render connections between nodes
    public void renderConnections() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED); // Color for the connection lines

        // Get all nodes in the graph
        List<PathNode> nodes = pathGraph.getNodes();

        for (PathNode node : nodes) {
            Vector2 nodePosition = new Vector2(node.x, node.y);

            // Iterate through neighbors and draw lines
            for (PathNode neighbor : node.neighbors) {
                Vector2 neighborPosition = new Vector2(neighbor.x, neighbor.y);
                // Draw a line between the current node and its neighbor
                shapeRenderer.line(nodePosition, neighborPosition);
            }
        }

        shapeRenderer.end();
    }

    // Render nodes as small circles
    public void renderNodes() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLUE); // Color for the nodes

        // Draw each node as a circle
        for (PathNode node : pathGraph.getNodes()) {
            shapeRenderer.circle(node.x, node.y, 5); // 5 is the radius of the node
        }

        shapeRenderer.end();
    }

    // You can call this in your render method to draw both the nodes and the connections
    public void render() {
        renderConnections();
        renderNodes();
    }
}


