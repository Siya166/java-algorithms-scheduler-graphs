package dev.tatenda.algorithms.graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A weighted graph stored as an adjacency list.
 *
 * <p>An adjacency list (a map from each vertex to its outgoing edges) uses O(V + E) memory,
 * which suits the sparse graphs found in most real problems (road maps, task dependencies).
 * An adjacency matrix would use O(V^2) memory regardless of how many edges exist.</p>
 *
 * <p>{@link LinkedHashMap} keeps vertices in insertion order, so traversals are deterministic
 * and easy to test.</p>
 *
 * @param <T> the vertex type; must implement equals and hashCode correctly
 */
public final class Graph<T> {

    /** A directed, weighted edge to {@code target}. */
    public record Edge<T>(T target, double weight) {
        public Edge {
            Objects.requireNonNull(target, "target");
            if (weight < 0 || Double.isNaN(weight)) {
                throw new IllegalArgumentException("Edge weights must be non-negative: " + weight);
            }
        }
    }

    private final Map<T, List<Edge<T>>> adjacency = new LinkedHashMap<>();
    private final boolean directed;

    private Graph(boolean directed) {
        this.directed = directed;
    }

    /** Creates an empty directed graph: an edge a to b does not imply b to a. */
    public static <T> Graph<T> directed() {
        return new Graph<>(true);
    }

    /** Creates an empty undirected graph: every edge is stored in both directions. */
    public static <T> Graph<T> undirected() {
        return new Graph<>(false);
    }

    /** Adds a vertex if it is not already present. */
    public Graph<T> addVertex(T vertex) {
        Objects.requireNonNull(vertex, "vertex");
        adjacency.putIfAbsent(vertex, new ArrayList<>());
        return this;
    }

    /** Adds an edge with weight 1. */
    public Graph<T> addEdge(T from, T to) {
        return addEdge(from, to, 1.0);
    }

    /** Adds a weighted edge, creating either vertex if needed. */
    public Graph<T> addEdge(T from, T to, double weight) {
        addVertex(from);
        addVertex(to);
        adjacency.get(from).add(new Edge<>(to, weight));
        if (!directed && !from.equals(to)) {
            adjacency.get(to).add(new Edge<>(from, weight));
        }
        return this;
    }

    public boolean isDirected() {
        return directed;
    }

    public boolean containsVertex(T vertex) {
        return adjacency.containsKey(vertex);
    }

    /** Vertices in insertion order (read-only view). */
    public Set<T> vertices() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /** Outgoing edges of {@code vertex} (read-only view). */
    public List<Edge<T>> edgesFrom(T vertex) {
        requireVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    /** Neighbours of {@code vertex}, in the order their edges were added, without duplicates. */
    public Set<T> neighbours(T vertex) {
        Set<T> result = new LinkedHashSet<>();
        for (Edge<T> edge : edgesFrom(vertex)) {
            result.add(edge.target());
        }
        return result;
    }

    public int vertexCount() {
        return adjacency.size();
    }

    void requireVertex(T vertex) {
        if (!adjacency.containsKey(vertex)) {
            throw new IllegalArgumentException("Unknown vertex: " + vertex);
        }
    }
}
