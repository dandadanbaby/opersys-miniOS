package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

// here's our implementation of non-preemptive Priority Scheduling!
// our prof explained that lower numbers mean higher priority (like 1st place > 2nd place).
// since this version is non-preemptive, once a process gets CPU time, it holds onto it until it's done.
// if two jobs tie on priority, we pick whoever arrived first, and if that's also a tie, we sort by PID.
public class PriorityScheduler implements CpuScheduler {

    @Override
    public String getAlgorithmName() { return "Priority"; }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        // we make a copy so we don't accidentally mutate the input list
        List<Process> remaining = new ArrayList<>(processes);
        List<GanttEntry> gantt = new ArrayList<>();
        List<SchedulingResult> results = new ArrayList<>();
        double currentTime = 0;

        // keep going until every single process has run
        while (!remaining.isEmpty()) {
            // find all the processes that have actually arrived by currentTime
            List<Process> available = new ArrayList<>();
            for (Process p : remaining) {
                if (p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            // if nothing has arrived yet, the CPU is stuck waiting (idle)
            // so we skip time ahead to the next arrival and add an IDLE block to our Gantt chart
            if (available.isEmpty()) {
                double nextArrival = remaining.stream()
                        .mapToDouble(Process::getArrivalTime).min().orElse(currentTime);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // sort available jobs: smallest priority number first, then arrival time, then PID
            available.sort(Comparator.comparingInt(Process::getPriority)
                    .thenComparingDouble(Process::getArrivalTime)
                    .thenComparing(Process::getPid));

            // grab the winner with the highest priority and remove it from remaining
            Process selected = available.get(0);
            remaining.remove(selected);

            // since it's non-preemptive, it runs completely to the end
            // response time is just start time minus arrival time
            double startTime = currentTime;
            double completionTime = currentTime + selected.getBurstTime();
            double responseTime = startTime - selected.getArrivalTime();

            // add this process's execution block to the Gantt chart and save its metrics
            gantt.add(new GanttEntry(selected.getPid(), startTime, completionTime));
            results.add(new SchedulingResult(
                    selected.getPid(), selected.getArrivalTime(), selected.getBurstTime(),
                    selected.getPriority(), completionTime, responseTime
            ));

            currentTime = completionTime;
        }

        // quantum is 0 here since Priority doesn't use time slices like Round Robin
        return new SchedulingOutput(getAlgorithmName(), 0, results, gantt);
    }
}
