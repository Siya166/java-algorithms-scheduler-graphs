package dev.tatenda.algorithms.scheduler;

import java.util.List;

/** The output of {@link TaskScheduler#schedule}: every task with its slot, in start order. */
public record Schedule(List<ScheduledTask> entries) {

    public Schedule {
        entries = List.copyOf(entries);
    }

    /** Time at which the last task finishes. */
    public int makespan() {
        return entries.stream().mapToInt(ScheduledTask::end).max().orElse(0);
    }

    public long lateCount() {
        return entries.stream().filter(ScheduledTask::isLate).count();
    }

    public int totalLateness() {
        return entries.stream().mapToInt(ScheduledTask::lateness).sum();
    }

    public int maxLateness() {
        return entries.stream().mapToInt(ScheduledTask::lateness).max().orElse(0);
    }

    public double averageCompletionTime() {
        return entries.stream().mapToInt(ScheduledTask::end).average().orElse(0);
    }

    /** A plain-text table, useful for the demo and for debugging. */
    public String toTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-24s %6s %5s %5s %8s %5s%n",
                "Task", "Name", "Worker", "Start", "End", "Deadline", "Late"));
        for (ScheduledTask e : entries) {
            sb.append(String.format("%-6s %-24s %6d %5d %5d %8d %5s%n",
                    e.task().id(), e.task().name(), e.worker(), e.start(), e.end(),
                    e.task().deadline(), e.isLate() ? "yes" : ""));
        }
        sb.append(String.format("Makespan %d, late tasks %d, total lateness %d, average completion %.1f%n",
                makespan(), lateCount(), totalLateness(), averageCompletionTime()));
        return sb.toString();
    }
}
