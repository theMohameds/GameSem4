package io.group9.enemy.pathfinding;

import java.util.*;

public class AStar {

    // The heuristic function interface, so you can pass your own heuristic logic
    public interface Heuristic {
        double estimate(PathNode node, PathNode goal);
    }

    public Heuristic euclideanHeuristic = (node, goal) -> {
        double dx = node.x - goal.x;
        double dy = node.y - goal.y;
        return Math.sqrt(dx*dx + dy*dy);
    };

    public List<PathNode> aStarSearch(PathNode start, PathNode goal, Heuristic heuristic) {
        // PriorityQueue orders nodes by their fCost (lowest first)
        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(PathNode::fCost));

        Map<PathNode, PathNode> cameFrom = new HashMap<>();
        Map<PathNode, Double> gScore = new HashMap<>();
        Map<PathNode, Double> fScore = new HashMap<>();

        gScore.put(start, 0.0);
        start.gCost = 0;
        start.hCost = (float)heuristic.estimate(start, goal);
        fScore.put(start, (double)start.fCost());

        openSet.add(start);

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            for (PathNode neighbor : current.neighbors) {
                double tentative_gScore = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + distance(current, neighbor);

                if (tentative_gScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentative_gScore);

                    neighbor.gCost = (float)tentative_gScore;
                    neighbor.hCost = (float)heuristic.estimate(neighbor, goal);

                    fScore.put(neighbor, (double)neighbor.fCost());

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else {
                        // Remove and re-add to update priority (Java PQ doesn't auto-update)
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    private List<PathNode> reconstructPath(Map<PathNode, PathNode> cameFrom, PathNode current) {
        List<PathNode> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(0, current);  // prepend to path
        }
        return totalPath;
    }

    private double distance(PathNode a, PathNode b) {
        // Euclidean distance between nodes
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
