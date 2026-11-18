package com.beastos.cpu.model;

// this class holds all the final scheduling stats for a single process after the CPU finishes running it.
// we store the input specs plus the computed times so we can display them in our results table.
public class SchedulingResult {
    private final String pid;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;
    private final int completionTime;
    private final int turnaroundTime;
    private final int waitingTime;
    private final int responseTime;

    public SchedulingResult(String pid, int arrivalTime, int burstTime, int priority,
                            int completionTime, int responseTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.completionTime = completionTime;
        // standard formulas we learned in OS class:
        // turnaround time = completion - arrival (total time from entry to finish)
        // waiting time = turnaround - burst (total time spent just waiting around)
        this.turnaroundTime = completionTime - arrivalTime;
        this.waitingTime = turnaroundTime - burstTime;
        this.responseTime = responseTime;
    }

    public String getPid() { return pid; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getCompletionTime() { return completionTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getResponseTime() { return responseTime; }
}
