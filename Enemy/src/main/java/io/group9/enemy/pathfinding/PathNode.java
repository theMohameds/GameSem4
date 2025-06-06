package io.group9.enemy.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathNode {
    public float x, y;
    public float gCost, hCost;
    public List<PathNode> neighbors;

    public PathNode(float x, float y) {
        this.x = x;
        this.y = y;
        this.neighbors = new ArrayList<>();
    }

    public float fCost() {
        return gCost + hCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathNode)) return false;
        PathNode p = (PathNode)o;
        return this.x == p.x && this.y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
