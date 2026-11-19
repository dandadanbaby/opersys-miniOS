package com.beastos.multiprogramming.model;

import java.util.List;

public class MultiprogrammingOutput {
    private final int totalJobs;
    private final int maxConcurrency;     // peak nj
    private final double startTime;
    private final double endTime;
    private final double totalBurstWork;
    private final double cpuUtilization;  // %
    private final double avgTurnaround;
    private final List<ProgramProcess> jobs;
    private final List<String[]> traceRows; // [time, event, nj, %cpu/job, et, hw/job, active jobs & time left]

    public MultiprogrammingOutput(int totalJobs, int maxConcurrency, double startTime, double endTime,
                                  double totalBurstWork, double cpuUtilization, double avgTurnaround,
                                  List<ProgramProcess> jobs, List<String[]> traceRows) {
        this.totalJobs = totalJobs;
        this.maxConcurrency = maxConcurrency;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalBurstWork = totalBurstWork;
        this.cpuUtilization = cpuUtilization;
        this.avgTurnaround = avgTurnaround;
        this.jobs = List.copyOf(jobs);
        this.traceRows = List.copyOf(traceRows);
    }

    public int getTotalJobs() { return totalJobs; }
    public int getMaxConcurrency() { return maxConcurrency; }
    public double getStartTime() { return startTime; }
    public double getEndTime() { return endTime; }
    public double getTotalBurstWork() { return totalBurstWork; }
    public double getCpuUtilization() { return cpuUtilization; }
    public double getAvgTurnaround() { return avgTurnaround; }
    public List<ProgramProcess> getJobs() { return jobs; }
    public List<String[]> getTraceRows() { return traceRows; }
}
