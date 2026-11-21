package dev.tatenda.algorithms.scheduler;

import dev.tatenda.algorithms.graph.TopologicalSort;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskSchedulerTest {

    private static List<String> ids(Schedule s) {
        return s.entries().stream().map(e -> e.task().id()).toList();
    }

    @Test
    void highestPriorityRunsFirst() {
        List<Task> tasks = List.of(Task.of("low", "Low", 1, 1, 10), Task.of("high", "High", 1, 9, 10));
        Schedule s = new TaskScheduler(1, Strategy.HIGHEST_PRIORITY).schedule(tasks);
        assertEquals(List.of("high", "low"), ids(s));
    }

    @Test
    void earliestDeadlineRunsFirst() {
        List<Task> tasks = List.of(Task.of("late", "Late", 1, 1, 10), Task.of("soon", "Soon", 1, 1, 2));
        Schedule s = new TaskScheduler(1, Strategy.EARLIEST_DEADLINE).schedule(tasks);
        assertEquals(List.of("soon", "late"), ids(s));
    }

    @Test
    void shortestJobMinimisesAverageCompletion() {
        List<Task> tasks = List.of(Task.of("long", "Long", 10, 1, 99), Task.of("short", "Short", 1, 1, 99));
        Schedule sjf = new TaskScheduler(1, Strategy.SHORTEST_JOB).schedule(tasks);
        assertEquals(List.of("short", "long"), ids(sjf));
        assertEquals(6.0, sjf.averageCompletionTime()); // (1 + 11) / 2
    }

    @Test
    void dependenciesAreRespectedEvenAgainstPriority() {
        List<Task> tasks = List.of(
                Task.of("a", "Prepare", 2, 1, 10),
                new Task("b", "Urgent but blocked", 1, 100, 10, Set.of("a")));
        Schedule s = new TaskScheduler(1, Strategy.HIGHEST_PRIORITY).schedule(tasks);
        assertEquals(List.of("a", "b"), ids(s));
        assertEquals(2, s.entries().get(1).start());
    }

    @Test
    void workersRunTasksInParallel() {
        List<Task> tasks = List.of(
                Task.of("a", "A", 4, 1, 10), Task.of("b", "B", 4, 1, 10), Task.of("c", "C", 4, 1, 10));
        Schedule s = new TaskScheduler(3, Strategy.HIGHEST_PRIORITY).schedule(tasks);
        assertEquals(4, s.makespan());
        assertEquals(Set.of(1, 2, 3), Set.copyOf(s.entries().stream().map(ScheduledTask::worker).toList()));
    }

    @Test
    void noTaskStartsBeforeItsDependenciesFinish() {
        List<Task> tasks = List.of(
                Task.of("a", "A", 3, 1, 99),
                Task.of("b", "B", 1, 1, 99),
                new Task("c", "C", 2, 1, 99, Set.of("a", "b")),
                new Task("d", "D", 2, 1, 99, Set.of("b")),
                new Task("e", "E", 1, 1, 99, Set.of("c", "d")));
        Schedule s = new TaskScheduler(2, Strategy.SHORTEST_JOB).schedule(tasks);
        Map<String, ScheduledTask> byId = new HashMap<>();
        s.entries().forEach(e -> byId.put(e.task().id(), e));
        for (ScheduledTask e : s.entries()) {
            for (String dep : e.task().dependsOn()) {
                assertTrue(byId.get(dep).end() <= e.start(), dep + " must finish before " + e.task().id());
            }
        }
        assertEquals(5, s.entries().size());
    }

    @Test
    void noWorkerRunsTwoTasksAtOnce() {
        List<Task> tasks = List.of(
                Task.of("a", "A", 2, 1, 99), Task.of("b", "B", 3, 2, 99),
                Task.of("c", "C", 1, 3, 99), Task.of("d", "D", 4, 4, 99));
        Schedule s = new TaskScheduler(2, Strategy.HIGHEST_PRIORITY).schedule(tasks);
        for (ScheduledTask x : s.entries()) {
            for (ScheduledTask y : s.entries()) {
                if (x != y && x.worker() == y.worker()) {
                    assertTrue(x.end() <= y.start() || y.end() <= x.start(), "overlap on worker " + x.worker());
                }
            }
        }
    }

    @Test
    void latenessIsReported() {
        Schedule s = new TaskScheduler(1, Strategy.HIGHEST_PRIORITY)
                .schedule(List.of(Task.of("a", "A", 5, 1, 3)));
        assertEquals(1, s.lateCount());
        assertEquals(2, s.maxLateness());
    }

    @Test
    void cyclicDependenciesAreRejected() {
        List<Task> tasks = List.of(
                new Task("a", "A", 1, 1, 9, Set.of("b")),
                new Task("b", "B", 1, 1, 9, Set.of("a")));
        assertThrows(TopologicalSort.CycleDetectedException.class,
                () -> new TaskScheduler(1, Strategy.HIGHEST_PRIORITY).schedule(tasks));
    }

    @Test
    void unknownDependencyIsRejected() {
        List<Task> tasks = List.of(new Task("a", "A", 1, 1, 9, Set.of("ghost")));
        assertThrows(IllegalArgumentException.class,
                () -> new TaskScheduler(1, Strategy.HIGHEST_PRIORITY).schedule(tasks));
    }

    @Test
    void duplicateIdsAndBadInputAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new TaskScheduler(1, Strategy.SHORTEST_JOB)
                .schedule(List.of(Task.of("a", "A", 1, 1, 1), Task.of("a", "B", 1, 1, 1))));
        assertThrows(IllegalArgumentException.class, () -> Task.of("x", "X", 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new TaskScheduler(0, Strategy.SHORTEST_JOB));
        assertThrows(IllegalArgumentException.class, () -> new Task("x", "X", 1, 1, 1, Set.of("x")));
    }

    @Test
    void emptyInputGivesEmptySchedule() {
        Schedule s = new TaskScheduler(2, Strategy.EARLIEST_DEADLINE).schedule(List.of());
        assertEquals(0, s.makespan());
        assertTrue(s.entries().isEmpty());
    }
}
