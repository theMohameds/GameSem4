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
        float maxDistanceX = 32f;          // Max horizontal distance for walking
        float maxDistanceY = 1f;           // Max vertical tolerance for walking
        float maxJumpHeight = 64f;          // Adjust to your single jump height
        float maxDoubleJumpHeight = 128f;    // Adjust to your double jump height
        float maxJumpHorizontalDistance = 224f; // Max horizontal jump distance

        for (PathNode node : nodes) {
            node.neighbors.clear();
            for (PathNode other : nodes) {
                if (node == other) continue;

                float dx = other.x - node.x;
                float dy = other.y - node.y;

                // Walking (existing code)
                if (Math.abs(dy) <= maxDistanceY) {
                    if (Math.abs(dx) > 0 && Math.abs(dx) <= maxDistanceX) {
                        node.neighbors.add(other);
                        continue;
                    }
                }

                // Jumping (upwards)
                if (other.y > node.y) {
                    float horizontalDistance = Math.abs(dx);
                    float verticalDistance = dy;

                    // Single jump check
                    if (verticalDistance <= maxJumpHeight && horizontalDistance <= maxJumpHorizontalDistance) {
                        node.neighbors.add(other);
                        node.setRequiresDoubleJump(true);
                    }
                }

            }
        }
    }
}
