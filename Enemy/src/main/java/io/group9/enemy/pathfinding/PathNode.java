package io.group9.enemy.pathfinding;

import java.util.Objects;

public class PathNode {
    public final int x, y;
    public float g;
    public float h;
    public PathNode parent;
    public final float terrainCost;

    public PathNode(int x, int y) {
        this(x, y, 1f);
    }

    public PathNode(int x, int y, float terrainCost) {
        this.x = x;
        this.y = y;
        this.terrainCost = terrainCost;
        this.g = Float.POSITIVE_INFINITY;
        this.h = 0f;
        this.parent = null;
    }

    public float f() {
        return g + h;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PathNode)) return false;
        PathNode o = (PathNode)obj;
        return o.x == x && o.y == y;
    }
}

