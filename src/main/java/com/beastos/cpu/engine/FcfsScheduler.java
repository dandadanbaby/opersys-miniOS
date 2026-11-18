package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/*
 * Team notes:
 * This is our First Come First Serve (FCFS) scheduler!
 * It's the simplest scheduling algorithm — basically whoever shows up first gets the CPU.
 * It's non-preemptive, so once a process starts running, it gets to finish its whole burst.
 * If two processes arrive at the exact same time, we break the tie using PID so our results are consistent.
 */
public class FcfsScheduler implements CpuScheduler {

    @Override
    public String getAlgorithmName() { return "FCFS"; }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        // we copy the list and sort by arrival time so processes get handled in the order they arrive
        // if there's a tie, we sort by PID so our simulation has deterministic ordering
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getPid));

        List<GanttEntry> gantt = new ArrayList<>();
        List<SchedulingResult> results = new ArrayList<>();
        int currentTime = 0;

        for (Process p : sorted) {
            // if the CPU is free but the next process hasn't arrived yet, we mark that gap as IDLE
            if (currentTime < p.getArrivalTime()) {
                gantt.add(new GanttEntry("IDLE", currentTime, p.getArrivalTime()));
                currentTime = p.getArrivalTime();
            }

            int startTime = currentTime;
            int completionTime = currentTime + p.getBurstTime();

            gantt.add(new GanttEntry(p.getPid(), startTime, completionTime));

            results.add(new SchedulingResult(
                    p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                    completionTime, startTime - p.getArrivalTime() // response time is just when it first got the CPU minus arrival
            ));

            currentTime = completionTime;
        }

        return new SchedulingOutput(getAlgorithmName(), 0, results, gantt);
    }
}
