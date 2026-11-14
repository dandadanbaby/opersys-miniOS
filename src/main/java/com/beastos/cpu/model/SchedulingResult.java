package com.beastos.cpu.model;

/**
 * Per-process scheduling result with all computed metrics.
 */
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
