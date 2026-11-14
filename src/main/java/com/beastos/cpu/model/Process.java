package com.beastos.cpu.model;

/**
 * Represents a process for CPU scheduling.
 * Immutable input model.
 */
public class Process {
    private final String pid;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority; // lower value = higher priority

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    public Process(String pid, int arrivalTime, int burstTime) {
        this(pid, arrivalTime, burstTime, 0);
    }

    public String getPid() { return pid; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }

    @Override
    public String toString() {
        return pid + "(AT=" + arrivalTime + ", BT=" + burstTime + ", P=" + priority + ")";
    }
}
