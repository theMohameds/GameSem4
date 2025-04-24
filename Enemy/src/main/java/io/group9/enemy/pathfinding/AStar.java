package io.group9.enemy.pathfinding;

import java.util.*;

public class AStar {
    private static final int[][] DIRS = {
        { 1,  0}, {-1,  0}, { 0,  1}, { 0, -1},
        { 1,  1}, { 1, -1}, {-1,  1}, {-1, -1}
    };
    private static final float[] DIR_COST = {
        1f, 1f, 1f, 1f,
        (float)Math.sqrt(2), (float)Math.sqrt(2),
        (float)Math.sqrt(2), (float)Math.sqrt(2)
    };

    private final int rows, cols;
    private final PathNode[][] nodeGrid;
    private final boolean[][] closed;
    private final PriorityQueue<PathNode> open =
        new PriorityQueue<>(Comparator.comparingDouble(PathNode::f));

    public AStar(float[][] terrainCosts) {
        this.rows = terrainCosts.length;
        this.cols = terrainCosts[0].length;
        this.closed = new boolean[rows][cols];
        this.nodeGrid = new PathNode[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                nodeGrid[y][x] = new PathNode(x, y, terrainCosts[y][x]);
            }
        }
    }

    public List<PathNode> findPath(int sx, int sy, int gx, int gy) {
        if (!inBounds(sx, sy) || !inBounds(gx, gy)) {
            return Collections.emptyList();
        }

        // Reset open & closed
        open.clear();
        for (boolean[] row : closed) Arrays.fill(row, false);

        PathNode start = nodeGrid[sy][sx];
        PathNode goal  = nodeGrid[gy][gx];

        // Initialize start
        start.g = 0f;
        start.h = heuristic(start, goal);
        start.parent = null;
        open.add(start);

        while (!open.isEmpty()) {
            PathNode current = open.poll();
            if (current == goal) {
                List<PathNode> raw = reconstructPath(goal);
                return smoothPath(raw);
            }

            closed[current.y][current.x] = true;

            for (int i = 0; i < DIRS.length; i++) {
                int nx = current.x + DIRS[i][0];
                int ny = current.y + DIRS[i][1];
                if (!inBounds(nx, ny) || closed[ny][nx]) continue;

                PathNode neighbor = nodeGrid[ny][nx];
                float cost = DIR_COST[i] * neighbor.terrainCost;
                float tentativeG = current.g + cost;

                if (neighbor.parent == null || tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.h = heuristic(neighbor, goal);
                    if (!open.contains(neighbor)) {
                        open.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < cols && y < rows;
    }

    private float heuristic(PathNode a, PathNode b) {
        float dx = Math.abs(a.x - b.x),
            dy = Math.abs(a.y - b.y);
        float F = (float)(Math.sqrt(2) - 1);
        return (dx < dy)
            ? F*dx + dy
            : F*dy + dx;
    }

    private List<PathNode> reconstructPath(PathNode goal) {
        LinkedList<PathNode> path = new LinkedList<>();
        for (PathNode n = goal; n != null; n = n.parent) {
            path.addFirst(n);
        }
        return path;
    }

    private List<PathNode> smoothPath(List<PathNode> raw) {
        if (raw.size() <= 2) return raw;
        List<PathNode> out = new ArrayList<>();
        int idx = 0, n = raw.size();
        while (idx < n) {
            out.add(raw.get(idx));
            int nextIdx = idx + 1;
            for (int j = n - 1; j > nextIdx; j--) {
                if (collinear(raw.get(idx), raw.get(j), raw)) {
                    nextIdx = j;
                    break;
                }
            }
            idx = nextIdx;
        }
        return out;
    }

    private boolean collinear(PathNode a, PathNode b, List<PathNode> raw) {
        int dx = b.x - a.x, dy = b.y - a.y;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        float sx = dx / (float)steps, sy = dy / (float)steps;
        float x = a.x, y = a.y;
        for (int i = 1; i < steps; i++) {
            x += sx; y += sy;
            int ix = Math.round(x), iy = Math.round(y);
            boolean found = raw.stream()
                .anyMatch(p -> p.x == ix && p.y == iy);
            if (!found) return false;
        }
        return true;
    }
}
