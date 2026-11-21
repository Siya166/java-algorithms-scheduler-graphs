package dev.tatenda.algorithms.scheduler;

/** A task with its assigned worker and time slot. */
public record ScheduledTask(Task task, int worker, int start, int end) {

    /** True if the task finishes after its deadline. */
    public boolean isLate() {
        return end > task.deadline();
    }

    /** How many time units past the deadline the task finishes (0 if on time). */
    public int lateness() {
        return Math.max(0, end - task.deadline());
    }
}
