package com.beastos.cpu.engine;

import com.beastos.cpu.model.*;
import com.beastos.cpu.model.Process;

import java.util.*;

/* 
 * Round Robin (RR) CPU Scheduling Algorithm!
 * Basically, this is preemptive scheduling where every process gets a fixed slice of time (the time quantum).
 * Once that time is up, if the process isn't done yet, it gets kicked off the CPU and put at the back of the queue.
 *
 * We figured out this tricky edge case: if a running process gets preempted at the exact same time
 * a new process arrives, the NEW arrival gets placed into the ready queue FIRST before the preempted
 * one goes back in. Also, if multiple processes arrive at the same time, we break ties using lowest PID.
 */
public class RoundRobinScheduler implements CpuScheduler {

    private final double timeQuantum;

    public RoundRobinScheduler(double timeQuantum) {
        if (timeQuantum <= 0) throw new IllegalArgumentException("Time quantum must be > 0");
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String getAlgorithmName() { return "Round Robin"; }

    @Override
    public SchedulingOutput schedule(List<Process> processes) {
        // we need these maps to keep track of remaining burst time and look up process info quickly
        Map<String, Double> remainingBurst = new LinkedHashMap<>();
        Map<String, Process> processMap = new LinkedHashMap<>();
        Map<String, Double> firstResponseTime = new HashMap<>();

        for (Process p : processes) {
            remainingBurst.put(p.getPid(), p.getBurstTime());
            processMap.put(p.getPid(), p);
        }

        // sort by arrival time first, and if they arrive together, we break ties by lowest PID
        List<Process> sortedByArrival = new ArrayList<>(processes);
        sortedByArrival.sort(Comparator.comparingDouble(Process::getArrivalTime)
                .thenComparing(Process::getPid));

        Queue<String> readyQueue = new LinkedList<>();
        Set<String> inQueue = new HashSet<>();
        Set<String> completed = new HashSet<>();

        List<GanttEntry> gantt = new ArrayList<>();
        Map<String, Double> completionTimes = new HashMap<>();
        double currentTime = 0;

        // load all the processes that arrived right at time 0 into our ready queue
        for (Process p : sortedByArrival) {
            if (p.getArrivalTime() <= 0 && !inQueue.contains(p.getPid())) {
                readyQueue.add(p.getPid());
                inQueue.add(p.getPid());
            }
        }

        while (completed.size() < processes.size()) {
            if (readyQueue.isEmpty()) {
                // if ready queue is empty, CPU is idle! so we jump ahead to whenever the next process arrives
                double nextArrival = Double.MAX_VALUE;
                for (Process p : sortedByArrival) {
                    if (!completed.contains(p.getPid()) && !inQueue.contains(p.getPid())) {
                        nextArrival = Math.min(nextArrival, p.getArrivalTime());
                    }
                }
                if (nextArrival == Double.MAX_VALUE) break;
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;

                // now that we fast-forwarded time, add all processes that have arrived by now
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
            double remaining = remainingBurst.get(pid);
            double execTime = Math.min(remaining, timeQuantum);

            // response time is recorded the very first time the process gets the CPU
            if (!firstResponseTime.containsKey(pid)) {
                firstResponseTime.put(pid, currentTime);
            }

            double startTime = currentTime;
            double endTime = currentTime + execTime;
            gantt.add(new GanttEntry(pid, startTime, endTime));

            currentTime = endTime;
            remaining -= execTime;
            remainingBurst.put(pid, remaining);

            // this part is tricky: any new arrivals during this quantum must enter the queue BEFORE the current process re-enters
            for (Process p : sortedByArrival) {
                if (p.getArrivalTime() > startTime && p.getArrivalTime() <= currentTime
                        && !inQueue.contains(p.getPid()) && !completed.contains(p.getPid())) {
                    readyQueue.add(p.getPid());
                    inQueue.add(p.getPid());
                }
            }

            if (remaining > 1e-9) {
                // process didn't finish its burst, so send it to the back of the queue
                readyQueue.add(pid);
            } else {
                // process is completely done! save completion time and take it out of the queue set
                completed.add(pid);
                completionTimes.put(pid, currentTime);
                inQueue.remove(pid);
            }
        }

        // basically what happens here is we package up the final metrics (turnaround, waiting, response) for each process
        List<SchedulingResult> results = new ArrayList<>();
        for (Process p : processes) {
            double ct = completionTimes.getOrDefault(p.getPid(), currentTime);
            double rt = firstResponseTime.getOrDefault(p.getPid(), 0.0) - p.getArrivalTime();
            results.add(new SchedulingResult(
                    p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                    ct, rt
            ));
        }

        return new SchedulingOutput(getAlgorithmName(), timeQuantum, results, gantt);
    }
}
