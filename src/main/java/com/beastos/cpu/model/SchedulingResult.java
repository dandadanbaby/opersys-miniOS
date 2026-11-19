package com.beastos.cpu.model;

// this class holds all the final scheduling stats for a single process after the CPU finishes running it.
// we store the input specs plus the computed times so we can display them in our results table.
public class SchedulingResult {
    private final String pid;
    private final double arrivalTime;
    private final double burstTime;
    private final int priority;
    private final double completionTime;
    private final double turnaroundTime;
    private final double waitingTime;
    private final double responseTime;

    public SchedulingResult(String pid, double arrivalTime, double burstTime, int priority,
                            double completionTime, double responseTime) {
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
    public double getArrivalTime() { return arrivalTime; }
    public double getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public double getCompletionTime() { return completionTime; }
    public double getTurnaroundTime() { return turnaroundTime; }
    public double getWaitingTime() { return waitingTime; }
    public double getResponseTime() { return responseTime; }
}
