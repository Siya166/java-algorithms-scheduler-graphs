# Task Scheduling and Graph Traversal in Java

[![CI](https://github.com/Siya166/java-algorithms-scheduler-graphs/actions/workflows/ci.yml/badge.svg)](https://github.com/Siya166/java-algorithms-scheduler-graphs/actions/workflows/ci.yml)

Two data structures and algorithms projects in plain Java 21, with no libraries except JUnit:

- **Graph traversal:** a generic weighted graph with breadth-first search, depth-first search, fewest-hop paths, Dijkstra's shortest paths, connected components and topological sort with cycle detection.
- **Task scheduling:** a scheduler that assigns tasks with dependencies, durations, priorities and deadlines to one or more workers, using a choice of scheduling rules, and reports lateness and completion times.

31 unit tests run on every push through GitHub Actions.

## Quick start

You need Java 21 and Maven.

```bash
git clone https://github.com/Siya166/java-algorithms-scheduler-graphs.git
cd java-algorithms-scheduler-graphs
mvn verify                    # compile and run all tests
mvn -q compile exec:java      # run the demo
```

## What the demo shows

Shortest routes between Eastern Cape towns (approximate distances), and a valid order to take university modules given their prerequisites:

```
BFS from Gqeberha: [Gqeberha, Uitenhage, Jeffreys Bay, Makhanda, Kirkwood, Humansdorp, East London]
Shortest distance Humansdorp to East London: [Humansdorp, Jeffreys Bay, Gqeberha, Makhanda, East London] (405 km)
A valid order to take modules: [Programming 1, Database Systems, Data Structures, Advanced Programming, Advanced Data Structures]
```

The same seven tasks scheduled on one worker under each rule. The rule matters: scheduling by priority leaves two tasks late with 28 time units of total lateness, while earliest-deadline-first leaves one task late by 1.

| Strategy | Late tasks | Total lateness | Average completion time |
| --- | --- | --- | --- |
| Highest priority | 2 | 28 | 11.7 |
| Earliest deadline first | 1 | 1 | 9.3 |
| Shortest job first | 1 | 1 | 9.3 |

## Algorithms and complexity

| Algorithm | Class | Time | Notes |
| --- | --- | --- | --- |
| Breadth-first search | `GraphTraversal.breadthFirst` | O(V + E) | Queue; finds fewest-edge paths |
| Depth-first search | `GraphTraversal.depthFirst` | O(V + E) | Explicit stack, so no stack overflow on deep graphs (tested on a 100,000-vertex chain) |
| Fewest-hop path | `GraphTraversal.shortestPathByEdges` | O(V + E) | BFS with a parent map |
| Connected components | `GraphTraversal.connectedComponents` | O(V + E) | Undirected graphs |
| Dijkstra | `Dijkstra` | O((V + E) log V) | Binary heap with lazy deletion; rejects negative weights |
| Topological sort | `TopologicalSort` | O(V + E) | Kahn's algorithm; reports the vertices stuck on a cycle |
| Task scheduling | `TaskScheduler` | O(n log n + e) | Event-driven list scheduling on k workers |

## Design decisions

- **Adjacency list, not matrix.** Uses O(V + E) memory, which suits sparse graphs such as road networks and dependency graphs.
- **Generic vertices.** `Graph<T>` works with strings, integers or any type with proper `equals` and `hashCode`.
- **Deterministic output.** Vertices keep insertion order and scheduling ties are broken by task id, so tests can check exact orders.
- **Strategies are comparators.** `Strategy` is an enum of `Comparator<Task>` values. The scheduler only asks "which ready task is best?", so a new rule needs no change to the scheduler (open/closed principle).
- **Reuse.** The scheduler checks for circular dependencies with the same `TopologicalSort` used for graphs, instead of duplicating cycle detection.
- **Fail fast.** Unknown dependencies, duplicate ids, cycles, zero durations and negative weights all throw clear exceptions instead of producing wrong results.

## Project structure

```
src/main/java/dev/tatenda/algorithms/
  Demo.java                 runnable examples
  graph/                    Graph, GraphTraversal, Dijkstra, TopologicalSort
  scheduler/                Task, Strategy, TaskScheduler, Schedule, ScheduledTask
src/test/java/...           JUnit 5 tests for every class
.github/workflows/ci.yml    runs mvn verify on every push
```

## What I would add next

- A* search with a distance heuristic.
- Preemptive scheduling (pausing a task when a more urgent one arrives).
- Reading tasks from a CSV file and exporting the schedule as a Gantt chart.

## Licence

MIT
