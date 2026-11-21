package dev.tatenda.algorithms.scheduler;

import java.util.Objects;
import java.util.Set;

/**
 * A unit of work to schedule.
 *
 * @param id        unique identifier, used by other tasks to declare dependencies
 * @param name      human-readable description
 * @param duration  time units needed to finish (must be positive)
 * @param priority  larger numbers are more important
 * @param deadline  time by which the task should finish
 * @param dependsOn ids of tasks that must finish before this one can start
 */
public record Task(String id, String name, int duration, int priority, int deadline, Set<String> dependsOn) {

    public Task {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive for task " + id);
        }
        dependsOn = Set.copyOf(dependsOn);
        if (dependsOn.contains(id)) {
            throw new IllegalArgumentException("Task " + id + " cannot depend on itself");
        }
    }

    /** Convenience factory for tasks without dependencies. */
    public static Task of(String id, String name, int duration, int priority, int deadline) {
        return new Task(id, name, duration, priority, deadline, Set.of());
    }
}
