package dev.tatenda.algorithms.scheduler;

import dev.tatenda.algorithms.graph.Graph;
import dev.tatenda.algorithms.graph.TopologicalSort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Schedules tasks with dependencies onto a fixed number of identical workers.
 *
 * <p>This is a discrete-event simulation of list scheduling: whenever a worker is free, it
 * takes the best <em>ready</em> task (all dependencies finished) according to the chosen
 * {@link Strategy}. If nothing is ready, time jumps to the next moment a running task finishes.
 * Each task is started once and each completion is processed once, so with a heap for the ready
 * set the run time is O(n log n + e), where e is the number of dependency edges.</p>
 *
 * <p>Before scheduling, the dependencies are checked with a topological sort; a cycle (A needs
 * B, B needs A) would otherwise make the scheduler wait forever.</p>
 */
public final class TaskScheduler {

    private final int workers;
    private final Strategy strategy;

    public TaskScheduler(int workers, Strategy strategy) {
        if (workers < 1) {
            throw new IllegalArgumentException("Need at least one worker");
        }
        this.workers = workers;
        this.strategy = strategy;
    }

    private record Running(ScheduledTask slot) {
    }

    public Schedule schedule(Collection<Task> tasks) {
        Map<String, Task> byId = index(tasks);
        validateDependencies(byId);

        // How many unfinished dependencies each task still waits on, and who waits on whom.
        Map<String, Integer> waitingOn = new HashMap<>();
        Map<String, List<Task>> dependents = new HashMap<>();
        PriorityQueue<Task> ready = new PriorityQueue<>(strategy.order());
        for (Task task : byId.values()) {
            waitingOn.put(task.id(), task.dependsOn().size());
            for (String dep : task.dependsOn()) {
                dependents.computeIfAbsent(dep, k -> new ArrayList<>()).add(task);
            }
            if (task.dependsOn().isEmpty()) {
                ready.add(task);
            }
        }

        // Running tasks ordered by finish time, then worker number.
        PriorityQueue<Running> running = new PriorityQueue<>((a, b) -> a.slot().end() != b.slot().end()
                ? Integer.compare(a.slot().end(), b.slot().end())
                : Integer.compare(a.slot().worker(), b.slot().worker()));
        PriorityQueue<Integer> freeWorkers = new PriorityQueue<>();
        for (int w = 1; w <= workers; w++) {
            freeWorkers.add(w);
        }

        List<ScheduledTask> result = new ArrayList<>();
        int time = 0;
        while (result.size() < byId.size()) {
            while (!freeWorkers.isEmpty() && !ready.isEmpty()) {
                Task next = ready.poll();
                ScheduledTask slot = new ScheduledTask(next, freeWorkers.poll(), time, time + next.duration());
                result.add(slot);
                running.add(new Running(slot));
            }
            if (running.isEmpty()) {
                break; // cannot happen after validation, but guards against an infinite loop
            }
            // Advance to the next completion and release everything finishing at that moment.
            time = running.peek().slot().end();
            while (!running.isEmpty() && running.peek().slot().end() == time) {
                ScheduledTask done = running.poll().slot();
                freeWorkers.add(done.worker());
                for (Task dependent : dependents.getOrDefault(done.task().id(), List.of())) {
                    if (waitingOn.merge(dependent.id(), -1, Integer::sum) == 0) {
                        ready.add(dependent);
                    }
                }
            }
        }
        return new Schedule(result);
    }

    private static Map<String, Task> index(Collection<Task> tasks) {
        Map<String, Task> byId = new LinkedHashMap<>();
        for (Task task : tasks) {
            if (byId.put(task.id(), task) != null) {
                throw new IllegalArgumentException("Duplicate task id: " + task.id());
            }
        }
        return byId;
    }

    private static void validateDependencies(Map<String, Task> byId) {
        Graph<String> graph = Graph.directed();
        for (Task task : byId.values()) {
            graph.addVertex(task.id());
            for (String dep : task.dependsOn()) {
                if (!byId.containsKey(dep)) {
                    throw new IllegalArgumentException("Task " + task.id() + " depends on unknown task " + dep);
                }
                graph.addEdge(dep, task.id());
            }
        }
        TopologicalSort.sort(graph); // throws CycleDetectedException if there is a cycle
    }
}
