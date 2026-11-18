package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/*
 * Our implementation of Non-Preemptive Shortest Job First (SJF).
 * Basically, whenever the CPU is ready for a new process, we check all the processes
 * that have arrived so far and pick the one with the shortest burst time.
 * Since it's non-preemptive, once a process starts running, it gets to finish completely.
 * If there's a tie, we pick the one that arrived earlier, or lowest PID as a last resort.
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
            // find all the processes that have actually arrived by the current time
            List<Process> available = new ArrayList<>();
            for (Process p : remaining) {
                if (p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                // if nothing has arrived yet, the CPU is just sitting idle!
                // we fast-forward our clock to when the next process is scheduled to arrive
                int nextArrival = remaining.stream()
                        .mapToInt(Process::getArrivalTime).min().orElse(currentTime);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // here's where we pick the shortest job!
            // sort by burst time first, then break ties using arrival time and PID
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
