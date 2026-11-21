package dev.tatenda.algorithms.scheduler;

import java.util.Comparator;

/**
 * Rules for choosing which ready task to start next.
 *
 * <p>Each strategy is just a {@link Comparator}; the scheduler does not know which rule it is
 * using. New rules can be added without changing {@link TaskScheduler} (open/closed principle).
 * Ties are always broken by task id so results are deterministic.</p>
 */
public enum Strategy {

    /** Highest priority first. */
    HIGHEST_PRIORITY(Comparator.comparingInt(Task::priority).reversed()),

    /** Earliest deadline first (EDF): minimises maximum lateness on a single worker. */
    EARLIEST_DEADLINE(Comparator.comparingInt(Task::deadline)),

    /** Shortest job first: minimises average completion time on a single worker. */
    SHORTEST_JOB(Comparator.comparingInt(Task::duration));

    private final Comparator<Task> order;

    Strategy(Comparator<Task> order) {
        this.order = order.thenComparing(Task::id);
    }

    public Comparator<Task> order() {
        return order;
    }
}
