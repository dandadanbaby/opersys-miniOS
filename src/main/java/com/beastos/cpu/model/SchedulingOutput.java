package com.beastos.cpu.model;

import java.util.List;

/**
 * Complete output of a CPU scheduling algorithm execution.
 */
public class SchedulingOutput {
    private final String algorithmName;
    private final int timeQuantum; // 0 if not applicable
    private final List<SchedulingResult> results;
    private final List<GanttEntry> ganttChart;
    private final double averageWaitingTime;
    private final double averageTurnaroundTime;
    private final double averageResponseTime;
    private final double cpuUtilization; // percentage
    private final int totalTime;

    public SchedulingOutput(String algorithmName, int timeQuantum,
                            List<SchedulingResult> results, List<GanttEntry> ganttChart) {
        this.algorithmName = algorithmName;
        this.timeQuantum = timeQuantum;
        this.results = List.copyOf(results);
        this.ganttChart = List.copyOf(ganttChart);

        // Calculate averages
        double sumWT = 0, sumTAT = 0, sumRT = 0;
        for (SchedulingResult r : results) {
            sumWT += r.getWaitingTime();
            sumTAT += r.getTurnaroundTime();
            sumRT += r.getResponseTime();
        }
        int n = results.size();
        this.averageWaitingTime = n > 0 ? sumWT / n : 0;
        this.averageTurnaroundTime = n > 0 ? sumTAT / n : 0;
        this.averageResponseTime = n > 0 ? sumRT / n : 0;

        // Calculate CPU utilization and total time
        int lastEnd = 0;
        int idleTime = 0;
        for (GanttEntry g : ganttChart) {
            if (g.isIdle()) idleTime += g.getDuration();
            lastEnd = Math.max(lastEnd, g.getEndTime());
        }
        this.totalTime = lastEnd;
        this.cpuUtilization = lastEnd > 0 ? ((double)(lastEnd - idleTime) / lastEnd) * 100.0 : 0;
    }

    public String getAlgorithmName() { return algorithmName; }
    public int getTimeQuantum() { return timeQuantum; }
    public List<SchedulingResult> getResults() { return results; }
    public List<GanttEntry> getGanttChart() { return ganttChart; }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getCpuUtilization() { return cpuUtilization; }
    public int getTotalTime() { return totalTime; }
    public int getProcessCount() { return results.size(); }
}
