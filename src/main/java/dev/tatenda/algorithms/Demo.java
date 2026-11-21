package dev.tatenda.algorithms;

import dev.tatenda.algorithms.graph.Dijkstra;
import dev.tatenda.algorithms.graph.Graph;
import dev.tatenda.algorithms.graph.GraphTraversal;
import dev.tatenda.algorithms.graph.TopologicalSort;
import dev.tatenda.algorithms.scheduler.Schedule;
import dev.tatenda.algorithms.scheduler.Strategy;
import dev.tatenda.algorithms.scheduler.Task;
import dev.tatenda.algorithms.scheduler.TaskScheduler;

import java.util.List;
import java.util.Set;

/** Runs the algorithms on small, realistic examples. Start with {@code mvn -q compile exec:java}. */
public final class Demo {

    private Demo() {
    }

    public static void main(String[] args) {
        graphDemo();
        System.out.println();
        schedulerDemo();
    }

    private static void graphDemo() {
        // Approximate driving distances in km between Eastern Cape towns (illustrative values).
        Graph<String> roads = Graph.<String>undirected()
                .addEdge("Gqeberha", "Uitenhage", 35)
                .addEdge("Gqeberha", "Jeffreys Bay", 75)
                .addEdge("Gqeberha", "Makhanda", 130)
                .addEdge("Jeffreys Bay", "Humansdorp", 20)
                .addEdge("Uitenhage", "Kirkwood", 60)
                .addEdge("Makhanda", "East London", 180)
                .addEdge("Kirkwood", "Makhanda", 120);

        System.out.println("== Graph traversal ==");
        System.out.println("BFS from Gqeberha: " + GraphTraversal.breadthFirst(roads, "Gqeberha"));
        System.out.println("DFS from Gqeberha: " + GraphTraversal.depthFirst(roads, "Gqeberha"));
        System.out.println("Fewest hops Humansdorp to East London: "
                + GraphTraversal.shortestPathByEdges(roads, "Humansdorp", "East London").orElseThrow());
        Dijkstra.shortestPath(roads, "Humansdorp", "East London").ifPresent(p ->
                System.out.printf("Shortest distance Humansdorp to East London: %s (%.0f km)%n",
                        p.vertices(), p.totalWeight()));

        Graph<String> modules = Graph.<String>directed()
                .addEdge("Programming 1", "Data Structures")
                .addEdge("Data Structures", "Advanced Data Structures")
                .addEdge("Programming 1", "Advanced Programming")
                .addEdge("Database Systems", "Advanced Programming");
        System.out.println("A valid order to take modules: " + TopologicalSort.sort(modules));
    }

    private static void schedulerDemo() {
        List<Task> tasks = List.of(
                Task.of("T1", "Collect data", 3, 5, 5),
                new Task("T2", "Clean data", 2, 4, 8, Set.of("T1")),
                new Task("T3", "Fit models", 4, 5, 14, Set.of("T2")),
                Task.of("T4", "Write lecture slides", 2, 3, 4),
                Task.of("T5", "Mark tests", 5, 2, 20),
                new Task("T6", "Write report", 3, 4, 18, Set.of("T3")),
                Task.of("T7", "Reply to emails", 1, 1, 2));

        System.out.println("== Task scheduling (1 worker) ==");
        for (Strategy strategy : Strategy.values()) {
            Schedule schedule = new TaskScheduler(1, strategy).schedule(tasks);
            System.out.println("Strategy: " + strategy);
            System.out.print(schedule.toTable());
            System.out.println();
        }
    }
}
