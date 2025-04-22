package io.group9.enemy.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

public class GridGraph implements IndexedGraph<PathNode> {

    private final int width, height;
    private final float cellSize;
    private final Array<PathNode> nodes = new Array<>();
    private final Array<Array<Connection<PathNode>>> connections = new Array<>();

    public GridGraph(int width, int height, float cellSize) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                nodes.add(new PathNode(x, y));
                connections.add(new Array<>(4));
            }
        buildConnections();
    }

    /* ---- connectivity ---- */
    private void buildConnections() {
        for (PathNode n : nodes) {
            if (isBlocked(n.x, n.y)) continue;
            Array<Connection<PathNode>> list = connections.get(getIndex(n));

            addIfWalkable(n.x + 1, n.y, n, list);
            addIfWalkable(n.x - 1, n.y, n, list);
            addIfWalkable(n.x, n.y + 1, n, list);
            addIfWalkable(n.x, n.y - 1, n, list);
        }
    }

    private void addIfWalkable(int x, int y, PathNode from, Array<Connection<PathNode>> list) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        if (isBlocked(x, y)) return;
        PathNode to = nodes.get(index(x, y));
        list.add(new DefaultConnection<>(from, to));
    }

    protected boolean isBlocked(int x, int y) { return false; }

    /* ---- IndexedGraph ---- */
    private int index(int x, int y) { return y * width + x; }

    @Override public int getIndex(PathNode node) { return index(node.x, node.y); }

    @Override public int getNodeCount() { return nodes.size; }

    @Override public Array<Connection<PathNode>> getConnections(PathNode node) {
        return connections.get(getIndex(node));
    }

    /* helper: convert grid to world coords */
    public float worldX(int gridX) { return (gridX + 0.5f) * cellSize; }
    public float worldY(int gridY) { return (gridY + 0.5f) * cellSize; }
    public float getCellSize() { return cellSize; }
}
