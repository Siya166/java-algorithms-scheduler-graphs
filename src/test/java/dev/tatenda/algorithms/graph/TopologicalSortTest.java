package dev.tatenda.algorithms.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologicalSortTest {

    @Test
    void everyEdgePointsForwardInTheOrder() {
        Graph<String> g = Graph.<String>directed()
                .addEdge("shirt", "tie").addEdge("tie", "jacket")
                .addEdge("trousers", "shoes").addEdge("trousers", "belt")
                .addEdge("belt", "jacket").addEdge("socks", "shoes");
        List<String> order = TopologicalSort.sort(g);
        assertEquals(g.vertexCount(), order.size());
        for (String u : g.vertices()) {
            for (Graph.Edge<String> e : g.edgesFrom(u)) {
                assertTrue(order.indexOf(u) < order.indexOf(e.target()), u + " should come before " + e.target());
            }
        }
    }

    @Test
    void cycleIsDetected() {
        Graph<String> g = Graph.<String>directed()
                .addEdge("A", "B").addEdge("B", "C").addEdge("C", "A").addEdge("X", "A");
        TopologicalSort.CycleDetectedException ex =
                assertThrows(TopologicalSort.CycleDetectedException.class, () -> TopologicalSort.sort(g));
        assertEquals(List.of("A", "B", "C"), ex.remaining());
    }

    @Test
    void undirectedGraphIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TopologicalSort.sort(Graph.<String>undirected().addEdge("A", "B")));
    }
}
