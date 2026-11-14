package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/**
 * Round Robin (RR) CPU Scheduling Algorithm.
 * Preemptive with configurable time quantum.
 * Uses arrival-aware ready queue. Processes enter the queue upon arrival.
 * Tie-breaking for simultaneous arrivals: lowest PID first.
 *
 * When a process is preempted and new processes arrive at the same time,
 * the newly arrived processes are added to the queue before the preempted process.
 */
public class RoundRobinScheduler implements CpuScheduler {

    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) throw new IllegalArgumentException("Time quantum must be > 0");
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String getAlgorithmName() { return "Round Robin"; }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        // Track remaining burst time per process
        Map<String, Integer> remainingBurst = new LinkedHashMap<>();
        Map<String, Process> processMap = new LinkedHashMap<>();
        Map<String, Integer> firstResponseTime = new HashMap<>();

        for (Process p : processes) {
            remainingBurst.put(p.getPid(), p.getBurstTime());
            processMap.put(p.getPid(), p);
        }

        // Sort by arrival time, then PID for initial ordering
        List<Process> sortedByArrival = new ArrayList<>(processes);
        sortedByArrival.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getPid));

        Queue<String> readyQueue = new LinkedList<>();
        Set<String> inQueue = new HashSet<>();
        Set<String> completed = new HashSet<>();

        List<GanttEntry> gantt = new ArrayList<>();
        Map<String, Integer> completionTimes = new HashMap<>();
        int currentTime = 0;

        // Add processes arriving at time 0
        for (Process p : sortedByArrival) {
            if (p.getArrivalTime() == 0 && !inQueue.contains(p.getPid())) {
                readyQueue.add(p.getPid());
                inQueue.add(p.getPid());
            }
        }

        while (completed.size() < processes.size()) {
            if (readyQueue.isEmpty()) {
                // Find next arrival
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : sortedByArrival) {
                    if (!completed.contains(p.getPid()) && !inQueue.contains(p.getPid())) {
                        nextArrival = Math.min(nextArrival, p.getArrivalTime());
                    }
                }
                if (nextArrival == Integer.MAX_VALUE) break;
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;

                // Add newly arrived processes
                for (Process p : sortedByArrival) {
                    if (p.getArrivalTime() <= currentTime && !inQueue.contains(p.getPid())
                            && !completed.contains(p.getPid())) {
                        readyQueue.add(p.getPid());
                        inQueue.add(p.getPid());
                    }
                }
                continue;
            }

            String pid = readyQueue.poll();
            int remaining = remainingBurst.get(pid);
            int execTime = Math.min(remaining, timeQuantum);

            // Record first response time
            if (!firstResponseTime.containsKey(pid)) {
                firstResponseTime.put(pid, currentTime);
            }

            int startTime = currentTime;
            int endTime = currentTime + execTime;
            gantt.add(new GanttEntry(pid, startTime, endTime));

            currentTime = endTime;
            remaining -= execTime;
            remainingBurst.put(pid, remaining);

            // Add newly arrived processes BEFORE the preempted process re-enters queue
            for (Process p : sortedByArrival) {
                if (p.getArrivalTime() > startTime && p.getArrivalTime() <= currentTime
                        && !inQueue.contains(p.getPid()) && !completed.contains(p.getPid())) {
                    readyQueue.add(p.getPid());
                    inQueue.add(p.getPid());
                }
            }

            if (remaining > 0) {
                // Process not finished — re-enter queue
                readyQueue.add(pid);
            } else {
                // Process completed
                completed.add(pid);
                completionTimes.put(pid, currentTime);
                inQueue.remove(pid);
            }
        }

        // Build results
        List<SchedulingResult> results = new ArrayList<>();
        for (Process p : processes) {
            int ct = completionTimes.getOrDefault(p.getPid(), currentTime);
            int rt = firstResponseTime.getOrDefault(p.getPid(), 0) - p.getArrivalTime();
            results.add(new SchedulingResult(
                    p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                    ct, rt
            ));
        }

        return new SchedulingOutput(getAlgorithmName(), timeQuantum, results, gantt);
    }
}
