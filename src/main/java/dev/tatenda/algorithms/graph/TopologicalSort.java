package dev.tatenda.algorithms.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Topological ordering of a directed acyclic graph using Kahn's algorithm.
 *
 * <p>Repeatedly removes a vertex with no incoming edges. If vertices remain but none has
 * in-degree zero, the graph contains a cycle and no valid ordering exists. O(V + E).</p>
 */
public final class TopologicalSort {

    /** Thrown when the graph has a cycle, so no topological order exists. */
    public static final class CycleDetectedException extends RuntimeException {
        private final List<?> remaining;

        CycleDetectedException(List<?> remaining) {
            super("Graph contains a cycle involving: " + remaining);
            this.remaining = List.copyOf(remaining);
        }

        /** Vertices that could not be ordered (each is on, or downstream of, a cycle). */
        public List<?> remaining() {
            return remaining;
        }
    }

    private TopologicalSort() {
    }

    /**
     * Orders vertices so that for every edge u to v, u comes before v.
     *
     * @throws CycleDetectedException if the graph has a cycle
     */
    public static <T> List<T> sort(Graph<T> graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort needs a directed graph");
        }
        Map<T, Integer> inDegree = new HashMap<>();
        for (T vertex : graph.vertices()) {
            inDegree.putIfAbsent(vertex, 0);
            for (Graph.Edge<T> edge : graph.edgesFrom(vertex)) {
                inDegree.merge(edge.target(), 1, Integer::sum);
            }
        }

        Deque<T> ready = new ArrayDeque<>();
        for (T vertex : graph.vertices()) {
            if (inDegree.get(vertex) == 0) {
                ready.add(vertex);
            }
        }

        List<T> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            T vertex = ready.poll();
            order.add(vertex);
            for (Graph.Edge<T> edge : graph.edgesFrom(vertex)) {
                if (inDegree.merge(edge.target(), -1, Integer::sum) == 0) {
                    ready.add(edge.target());
                }
            }
        }

        if (order.size() != graph.vertexCount()) {
            List<T> remaining = new ArrayList<>();
            for (T vertex : graph.vertices()) {
                if (inDegree.get(vertex) > 0) {
                    remaining.add(vertex);
                }
            }
            throw new CycleDetectedException(remaining);
        }
        return order;
    }
}
