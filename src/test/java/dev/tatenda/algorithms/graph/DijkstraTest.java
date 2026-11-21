package dev.tatenda.algorithms.graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DijkstraTest {

    @Test
    void prefersLighterPathWithMoreEdges() {
        Graph<String> g = Graph.<String>directed()
                .addEdge("S", "T", 10)
                .addEdge("S", "A", 2)
                .addEdge("A", "B", 2)
                .addEdge("B", "T", 2);
        Dijkstra.Path<String> path = Dijkstra.shortestPath(g, "S", "T").orElseThrow();
        assertEquals(List.of("S", "A", "B", "T"), path.vertices());
        assertEquals(6.0, path.totalWeight());
    }

    @Test
    void distancesMatchHandCalculation() {
        // Classic textbook example.
        Graph<Character> g = Graph.<Character>undirected()
                .addEdge('A', 'B', 4).addEdge('A', 'C', 2)
                .addEdge('B', 'C', 1).addEdge('B', 'D', 5)
                .addEdge('C', 'D', 8).addEdge('C', 'E', 10)
                .addEdge('D', 'E', 2);
        Map<Character, Double> d = Dijkstra.distancesFrom(g, 'A');
        assertEquals(0.0, d.get('A'));
        assertEquals(3.0, d.get('B'));
        assertEquals(2.0, d.get('C'));
        assertEquals(8.0, d.get('D'));
        assertEquals(10.0, d.get('E'));
    }

    @Test
    void unreachableTargetGivesEmptyResult() {
        Graph<String> g = Graph.<String>directed().addEdge("A", "B", 1).addVertex("C");
        assertTrue(Dijkstra.shortestPath(g, "A", "C").isEmpty());
        assertFalse(Dijkstra.distancesFrom(g, "A").containsKey("C"));
    }

    @Test
    void zeroWeightEdgesAreAllowed() {
        Graph<String> g = Graph.<String>directed().addEdge("A", "B", 0).addEdge("B", "C", 0);
        assertEquals(0.0, Dijkstra.shortestPath(g, "A", "C").orElseThrow().totalWeight());
    }

    @Test
    void negativeWeightsAreRejected() {
        Graph<String> g = Graph.directed();
        assertThrows(IllegalArgumentException.class, () -> g.addEdge("A", "B", -1));
    }
}
