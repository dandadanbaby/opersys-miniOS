package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/**
 * Priority CPU Scheduling Algorithm.
 * Non-preemptive. Lower priority number = higher priority.
 * Tie-breaking: earliest arrival, then lowest PID.
 */
public class PriorityScheduler implements CpuScheduler {

    @Override
    public String getAlgorithmName() { return "Priority"; }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        List<Process> remaining = new ArrayList<>(processes);
        List<GanttEntry> gantt = new ArrayList<>();
        List<SchedulingResult> results = new ArrayList<>();
        int currentTime = 0;

        while (!remaining.isEmpty()) {
            // Find arrived processes
            List<Process> available = new ArrayList<>();
            for (Process p : remaining) {
                if (p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                int nextArrival = remaining.stream()
                        .mapToInt(Process::getArrivalTime).min().orElse(currentTime);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // Select by priority (lowest number = highest priority)
            available.sort(Comparator.comparingInt(Process::getPriority)
                    .thenComparingInt(Process::getArrivalTime)
                    .thenComparing(Process::getPid));

            Process selected = available.get(0);
            remaining.remove(selected);

            int startTime = currentTime;
            int completionTime = currentTime + selected.getBurstTime();
            int responseTime = startTime - selected.getArrivalTime();

            gantt.add(new GanttEntry(selected.getPid(), startTime, completionTime));
            results.add(new SchedulingResult(
                    selected.getPid(), selected.getArrivalTime(), selected.getBurstTime(),
                    selected.getPriority(), completionTime, responseTime
            ));

            currentTime = completionTime;
        }

        return new SchedulingOutput(getAlgorithmName(), 0, results, gantt);
    }
}
