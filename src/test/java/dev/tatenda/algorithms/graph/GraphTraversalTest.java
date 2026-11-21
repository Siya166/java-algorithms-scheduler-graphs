package dev.tatenda.algorithms.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTraversalTest {

    /*
     *   A - B - D
     *   |   |
     *   C - E     F (isolated)
     */
    private Graph<String> sample() {
        return Graph.<String>undirected()
                .addEdge("A", "B").addEdge("A", "C")
                .addEdge("B", "D").addEdge("B", "E")
                .addEdge("C", "E")
                .addVertex("F");
    }

    @Test
    void breadthFirstVisitsLevelByLevel() {
        assertEquals(List.of("A", "B", "C", "D", "E"), GraphTraversal.breadthFirst(sample(), "A"));
    }

    @Test
    void depthFirstGoesDeepBeforeWide() {
        assertEquals(List.of("A", "B", "D", "E", "C"), GraphTraversal.depthFirst(sample(), "A"));
    }

    @Test
    void traversalOfIsolatedVertexReturnsOnlyItself() {
        assertEquals(List.of("F"), GraphTraversal.breadthFirst(sample(), "F"));
        assertEquals(List.of("F"), GraphTraversal.depthFirst(sample(), "F"));
    }

    @Test
    void traversalHandlesCycles() {
        Graph<Integer> cycle = Graph.<Integer>directed().addEdge(1, 2).addEdge(2, 3).addEdge(3, 1);
        assertEquals(List.of(1, 2, 3), GraphTraversal.breadthFirst(cycle, 1));
        assertEquals(List.of(1, 2, 3), GraphTraversal.depthFirst(cycle, 1));
    }

    @Test
    void depthFirstDoesNotOverflowOnLongChains() {
        Graph<Integer> chain = Graph.directed();
        for (int i = 0; i < 100_000; i++) {
            chain.addEdge(i, i + 1);
        }
        assertEquals(100_001, GraphTraversal.depthFirst(chain, 0).size());
    }

    @Test
    void shortestPathByEdgesFindsFewestHops() {
        assertEquals(List.of("D", "B", "E"),
                GraphTraversal.shortestPathByEdges(sample(), "D", "E").orElseThrow());
    }

    @Test
    void shortestPathToSelfIsSingleVertex() {
        assertEquals(List.of("A"), GraphTraversal.shortestPathByEdges(sample(), "A", "A").orElseThrow());
    }

    @Test
    void shortestPathIsEmptyWhenUnreachable() {
        assertTrue(GraphTraversal.shortestPathByEdges(sample(), "A", "F").isEmpty());
    }

    @Test
    void directedEdgesAreOneWay() {
        Graph<String> g = Graph.<String>directed().addEdge("X", "Y");
        assertTrue(GraphTraversal.shortestPathByEdges(g, "Y", "X").isEmpty());
    }

    @Test
    void connectedComponentsSplitsGraph() {
        List<List<String>> components = GraphTraversal.connectedComponents(sample());
        assertEquals(2, components.size());
        assertEquals(List.of("F"), components.get(1));
    }

    @Test
    void unknownStartVertexIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> GraphTraversal.breadthFirst(sample(), "Z"));
    }
}
