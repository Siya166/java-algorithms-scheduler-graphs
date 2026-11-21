package dev.tatenda.algorithms.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra's shortest-path algorithm for graphs with non-negative edge weights.
 *
 * <p>Runs in O((V + E) log V) using a binary-heap priority queue. Java's {@link PriorityQueue}
 * has no decrease-key operation, so instead of updating an entry we insert a new one and skip
 * stale entries when they are polled ("lazy deletion"). This is simpler and, in practice, fast.</p>
 *
 * <p>Negative weights are rejected by {@link Graph.Edge}, because Dijkstra's greedy choice is
 * only correct when a path can never get shorter by adding edges.</p>
 */
public final class Dijkstra {

    /** The result of a shortest path search. */
    public record Path<T>(List<T> vertices, double totalWeight) {
    }

    private record Entry<T>(T vertex, double distance) {
    }

    private Dijkstra() {
    }

    /** Shortest distance from {@code source} to every reachable vertex. */
    public static <T> Map<T, Double> distancesFrom(Graph<T> graph, T source) {
        return run(graph, source, null).distances;
    }

    /** Shortest weighted path from {@code source} to {@code target}, if one exists. */
    public static <T> Optional<Path<T>> shortestPath(Graph<T> graph, T source, T target) {
        graph.requireVertex(target);
        Result<T> result = run(graph, source, target);
        Double distance = result.distances.get(target);
        if (distance == null) {
            return Optional.empty();
        }
        return Optional.of(new Path<>(PathBuilder.build(result.parents, source, target), distance));
    }

    private record Result<T>(Map<T, Double> distances, Map<T, T> parents) {
    }

    private static <T> Result<T> run(Graph<T> graph, T source, T stopAt) {
        graph.requireVertex(source);
        Map<T, Double> distances = new HashMap<>();
        Map<T, T> parents = new HashMap<>();
        Set<T> settled = new HashSet<>();
        PriorityQueue<Entry<T>> queue =
                new PriorityQueue<>((a, b) -> Double.compare(a.distance(), b.distance()));

        distances.put(source, 0.0);
        queue.add(new Entry<>(source, 0.0));

        while (!queue.isEmpty()) {
            Entry<T> entry = queue.poll();
            T vertex = entry.vertex();
            if (!settled.add(vertex)) {
                continue; // stale entry: a shorter distance was already settled
            }
            if (vertex.equals(stopAt)) {
                break; // the target's distance is final once it is settled
            }
            for (Graph.Edge<T> edge : graph.edgesFrom(vertex)) {
                double candidate = entry.distance() + edge.weight();
                Double known = distances.get(edge.target());
                if (known == null || candidate < known) {
                    distances.put(edge.target(), candidate);
                    parents.put(edge.target(), vertex);
                    queue.add(new Entry<>(edge.target(), candidate));
                }
            }
        }
        return new Result<>(distances, parents);
    }
}
