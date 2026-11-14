package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/**
 * First Come First Serve (FCFS) CPU Scheduling Algorithm.
 * Non-preemptive. Processes executed in arrival order.
 * Tie-breaking: lowest PID first.
 */
public class FcfsScheduler implements CpuScheduler {

    @Override
    public String getAlgorithmName() { return "FCFS"; }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        // Sort by arrival time, then by PID for deterministic ordering
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getPid));

        List<GanttEntry> gantt = new ArrayList<>();
        List<SchedulingResult> results = new ArrayList<>();
        int currentTime = 0;

        for (Process p : sorted) {
            // CPU idle if process hasn't arrived yet
            if (currentTime < p.getArrivalTime()) {
                gantt.add(new GanttEntry("IDLE", currentTime, p.getArrivalTime()));
                currentTime = p.getArrivalTime();
            }

            int startTime = currentTime;
            int completionTime = currentTime + p.getBurstTime();

            gantt.add(new GanttEntry(p.getPid(), startTime, completionTime));

            results.add(new SchedulingResult(
                    p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                    completionTime, startTime - p.getArrivalTime() // response time
            ));

            currentTime = completionTime;
        }

        return new SchedulingOutput(getAlgorithmName(), 0, results, gantt);
    }
}
