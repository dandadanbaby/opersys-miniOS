package com.beastos.cpu.model;

// this is our basic process model representing a job to be scheduled on the CPU.
// we made all the fields final so it's immutable — that way, when scheduling algorithms
// simulate execution, they can't accidentally mutate the original input values!
public class Process {
    private final String pid;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority; // in our OS class convention, lower number = higher priority (0 is top priority)

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    // handy constructor for algorithms that don't care about priority (like FCFS or Round Robin)
    // we just default priority to 0 so we don't have to pass dummy values
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
