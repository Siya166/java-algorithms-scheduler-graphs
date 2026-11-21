package dev.tatenda.algorithms.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Breadth-first and depth-first search.
 *
 * <p>Both run in O(V + E) time: every vertex is visited once and every edge is examined once.
 * BFS explores level by level using a queue, so the first time it reaches a vertex it has
 * found a path with the fewest edges. DFS goes as deep as possible first using a stack.</p>
 */
public final class GraphTraversal {

    private GraphTraversal() {
    }

    /** Vertices reachable from {@code start}, in breadth-first order. */
    public static <T> List<T> breadthFirst(Graph<T> graph, T start) {
        graph.requireVertex(start);
        List<T> order = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Deque<T> queue = new ArrayDeque<>();

        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            order.add(current);
            for (T next : graph.neighbours(current)) {
                // Mark when enqueued, not when dequeued, so a vertex is never queued twice.
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return order;
    }

    /**
     * Vertices reachable from {@code start}, in depth-first (pre-order) order.
     *
     * <p>Uses an explicit stack rather than recursion, so very deep graphs cannot cause a
     * {@link StackOverflowError}. Neighbours are pushed in reverse so they are visited in the
     * same order a recursive DFS would visit them.</p>
     */
    public static <T> List<T> depthFirst(Graph<T> graph, T start) {
        graph.requireVertex(start);
        List<T> order = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();

        stack.push(start);
        while (!stack.isEmpty()) {
            T current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            order.add(current);
            List<T> neighbours = new ArrayList<>(graph.neighbours(current));
            Collections.reverse(neighbours);
            for (T next : neighbours) {
                if (!visited.contains(next)) {
                    stack.push(next);
                }
            }
        }
        return order;
    }

    /**
     * The path with the fewest edges from {@code start} to {@code goal}, ignoring weights.
     *
     * @return the path including both ends, or empty if {@code goal} is unreachable
     */
    public static <T> Optional<List<T>> shortestPathByEdges(Graph<T> graph, T start, T goal) {
        graph.requireVertex(start);
        graph.requireVertex(goal);
        Map<T, T> parent = new HashMap<>();
        Set<T> visited = new HashSet<>();
        Deque<T> queue = new ArrayDeque<>();

        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            if (current.equals(goal)) {
                return Optional.of(PathBuilder.build(parent, start, goal));
            }
            for (T next : graph.neighbours(current)) {
                if (visited.add(next)) {
                    parent.put(next, current);
                    queue.add(next);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Connected components of an undirected graph, each in BFS order.
     */
    public static <T> List<List<T>> connectedComponents(Graph<T> graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Connected components are defined here for undirected graphs");
        }
        List<List<T>> components = new ArrayList<>();
        Set<T> seen = new HashSet<>();
        for (T vertex : graph.vertices()) {
            if (!seen.contains(vertex)) {
                List<T> component = breadthFirst(graph, vertex);
                seen.addAll(component);
                components.add(component);
            }
        }
        return components;
    }
}
