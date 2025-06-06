package io.group9.enemy.pathfinding;

import java.util.ArrayList;
import java.util.List;

public class PathGraph {
    private List<PathNode> nodes = new ArrayList<>();

    public void addNode(PathNode node) {
        nodes.add(node);
    }

    public List<PathNode> getNodes() {
        return nodes;
    }

    public void connectNodes() {
        float maxWalkX = 32f;
        float maxWalkY = 20f;
        float maxJumpY = 60f;
        float maxDoubleJumpY = 120f;
        float maxJumpHorizontalDistance = 120f;

        for (PathNode node : nodes) {
            node.neighbors.clear();
        }

        for (PathNode node : nodes) {
            for (PathNode other : nodes) {
                if (node == other) continue;

                float dx = other.x - node.x;
                float dy = other.y - node.y;
                float absDx = Math.abs(dx);
                float absDy = Math.abs(dy);

                if (absDy <= maxWalkY && absDx > 0 && absDx <= maxWalkX) {
                    node.neighbors.add(other);
                }
            }
        }

        for (PathNode node : nodes) {
            for (PathNode other : nodes) {
                if (node == other) continue;
                if (node.neighbors.contains(other)) continue;

                float dx = other.x - node.x;
                float dy = other.y - node.y;
                float absDx = Math.abs(dx);
                float absDy = Math.abs(dy);

                if (absDy <= maxDoubleJumpY && absDx <= maxJumpHorizontalDistance) {
                    node.neighbors.add(other);
                }
            }
        }

    }

}
