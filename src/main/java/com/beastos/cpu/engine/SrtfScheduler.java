package com.beastos.cpu.engine;

import com.beastos.cpu.model.GanttEntry;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;

import java.util.*;

/*
 * SrtfScheduler - Shortest Remaining Time First (Preemptive SJF)!
 * Basically, whenever a new process arrives, we check if its burst time is strictly shorter
 * than whatever time the currently running process has left. If it is, the CPU immediately
 * preempts (pauses) the current process and switches to the new one!
 * Tie-breaker: earlier arrival time, then PID (e.g. P1 before P2).
 */
public class SrtfScheduler implements CpuScheduler {

    private static final double EPS = 1e-6;

    @Override
    public String getAlgorithmName() {
        return "SRTF";
    }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return new SchedulingOutput(getAlgorithmName(), 0, List.of(), List.of());
        }

        List<Process> pending = new ArrayList<>(processes);
        pending.sort(Comparator.comparingDouble(Process::getArrivalTime)
                .thenComparing(Process::getPid));

        Map<String, Double> remainingBurst = new HashMap<>();
        Map<String, Double> firstResponseTime = new HashMap<>();
        Map<String, Double> finishTimes = new HashMap<>();
        Map<String, Process> processMap = new HashMap<>();

        for (Process p : processes) {
            remainingBurst.put(p.getPid(), p.getBurstTime());
            processMap.put(p.getPid(), p);
        }

        List<GanttEntry> rawGantt = new ArrayList<>();
        double currentTime = 0.0;
        int completedCount = 0;
        int totalProcesses = processes.size();

        while (completedCount < totalProcesses) {
            // find all processes that have arrived by currentTime and still have burst left
            List<Process> available = new ArrayList<>();
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime + EPS && remainingBurst.get(p.getPid()) > EPS) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                // CPU is idle until the next pending process arrives
                double nextArrival = Double.POSITIVE_INFINITY;
                for (Process p : processes) {
                    if (remainingBurst.get(p.getPid()) > EPS && p.getArrivalTime() > currentTime) {
                        nextArrival = Math.min(nextArrival, p.getArrivalTime());
                    }
                }
                if (nextArrival == Double.POSITIVE_INFINITY) break;

                rawGantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // pick the process with the shortest remaining burst time!
            available.sort(Comparator.comparingDouble((Process p) -> remainingBurst.get(p.getPid()))
                    .thenComparingDouble(Process::getArrivalTime)
                    .thenComparing(Process::getPid));

            Process current = available.get(0);
            String pid = current.getPid();

            if (!firstResponseTime.containsKey(pid)) {
                firstResponseTime.put(pid, Math.max(0.0, currentTime - current.getArrivalTime()));
            }

            double rem = remainingBurst.get(pid);

            // figure out when the next event could happen:
            // either this process finishes, or a new process arrives that might preempt it
            double nextArrival = Double.POSITIVE_INFINITY;
            for (Process p : processes) {
                if (p.getArrivalTime() > currentTime + EPS && remainingBurst.get(p.getPid()) > EPS) {
                    nextArrival = Math.min(nextArrival, p.getArrivalTime());
                }
            }

            double timeSlice;
            if (nextArrival == Double.POSITIVE_INFINITY) {
                timeSlice = rem;
            } else {
                timeSlice = Math.min(rem, nextArrival - currentTime);
            }

            if (timeSlice < EPS) timeSlice = rem;

            double startTime = currentTime;
            double endTime = currentTime + timeSlice;
            rawGantt.add(new GanttEntry(pid, startTime, endTime));

            rem -= timeSlice;
            remainingBurst.put(pid, Math.max(0.0, rem));
            currentTime = endTime;

            if (remainingBurst.get(pid) <= EPS) {
                remainingBurst.put(pid, 0.0);
                finishTimes.put(pid, currentTime);
                completedCount++;
            }
        }

        // merge adjacent Gantt blocks for the same PID so connector arrows only show real preemptions
        List<GanttEntry> mergedGantt = mergeAdjacentEntries(rawGantt);

        List<SchedulingResult> results = new ArrayList<>();
        for (Process p : processes) {
            String pid = p.getPid();
            double ct = finishTimes.getOrDefault(pid, currentTime);
            double rt = firstResponseTime.getOrDefault(pid, 0.0);
            results.add(new SchedulingResult(pid, p.getArrivalTime(), p.getBurstTime(),
                    p.getPriority(), ct, rt));
        }

        return new SchedulingOutput(getAlgorithmName(), 0, results, mergedGantt);
    }

    private List<GanttEntry> mergeAdjacentEntries(List<GanttEntry> entries) {
        if (entries == null || entries.isEmpty()) return List.of();
        List<GanttEntry> merged = new ArrayList<>();
        GanttEntry current = entries.get(0);

        for (int i = 1; i < entries.size(); i++) {
            GanttEntry next = entries.get(i);
            if (current.getPid().equals(next.getPid())) {
                current = new GanttEntry(current.getPid(), current.getStartTime(), next.getEndTime());
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}
