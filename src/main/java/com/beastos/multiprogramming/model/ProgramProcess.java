package com.beastos.multiprogramming.model;

public class ProgramProcess {
    private final String pid;
    private final double arrivalTime; // ar
    private final double burstTime;   // bt
    private final ProcessStatus status;
    private final double finishTime;
    private final double turnaroundTime;

    public ProgramProcess(String pid, double arrivalTime, double burstTime) {
        this(pid, arrivalTime, burstTime, ProcessStatus.RUNNING, 0.0, 0.0);
    }

    public ProgramProcess(String pid, double arrivalTime, double burstTime,
                          ProcessStatus status, double finishTime, double turnaroundTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.status = status;
        this.finishTime = finishTime;
        this.turnaroundTime = turnaroundTime;
    }

    public String getPid() { return pid; }
    public double getArrivalTime() { return arrivalTime; }
    public double getBurstTime() { return burstTime; }
    public ProcessStatus getStatus() { return status; }
    public double getFinishTime() { return finishTime; }
    public double getTurnaroundTime() { return turnaroundTime; }
}
