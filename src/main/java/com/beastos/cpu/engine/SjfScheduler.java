package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/**
 * Shortest Job First (SJF) CPU Scheduling Algorithm.
 * Non-preemptive. Selects the arrived process with the shortest burst time.
 * Tie-breaking: earliest arrival, then lowest PID.
 */
public class SjfScheduler implements CpuScheduler {

    @Override
    public String getAlgorithmName() { return "SJF"; }

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
                // CPU idle — advance to next arrival
                int nextArrival = remaining.stream()
                        .mapToInt(Process::getArrivalTime).min().orElse(currentTime);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // Select process with shortest burst time
            available.sort(Comparator.comparingInt(Process::getBurstTime)
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
