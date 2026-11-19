package com.beastos.cpu.model;

import java.util.List;

// this class is basically our big bundle of everything a scheduling algorithm produces!
// once an algorithm finishes running, it packs up the per-process stats, the Gantt chart timeline,
// and we compute the big summary metrics (average WT, TAT, RT, and CPU utilization) right in the constructor
// so the UI can just grab this object and display the entire results screen easily.
public class SchedulingOutput {
    private final String algorithmName;
    private final double timeQuantum; // we set this to 0 if the algorithm doesn't use time slices (like FCFS or SJF)
    private final List<SchedulingResult> results;
    private final List<GanttEntry> ganttChart;
    private final double averageWaitingTime;
    private final double averageTurnaroundTime;
    private final double averageResponseTime;
    private final double cpuUtilization; // percentage of time the CPU was actually working (not idle)
    private final double totalTime;

    public SchedulingOutput(String algorithmName, double timeQuantum,
                            List<SchedulingResult> results, List<GanttEntry> ganttChart) {
        this.algorithmName = algorithmName;
        this.timeQuantum = timeQuantum;
        // we make unmodifiable copies so the chart data and results can't get accidentally modified later
        this.results = List.copyOf(results);
        this.ganttChart = List.copyOf(ganttChart);

        // basically what happens here is we loop through each process result and sum up
        // the waiting time, turnaround time, and response time so we can calculate class averages
        double sumWT = 0, sumTAT = 0, sumRT = 0;
        for (SchedulingResult r : results) {
            sumWT += r.getWaitingTime();
            sumTAT += r.getTurnaroundTime();
            sumRT += r.getResponseTime();
        }
        int n = results.size();
        // we check if n > 0 so we don't crash from dividing by zero if the list was empty lol
        this.averageWaitingTime = n > 0 ? sumWT / n : 0;
        this.averageTurnaroundTime = n > 0 ? sumTAT / n : 0;
        this.averageResponseTime = n > 0 ? sumRT / n : 0;

        // here we figure out the total simulation time and CPU utilization.
        // we scan the Gantt chart to find when the CPU finished everything and how much time was spent idle.
        // formula from our OS class: CPU utilization = ((total time - idle time) / total time) * 100%
        double lastEnd = 0;
        double idleTime = 0;
        for (GanttEntry g : ganttChart) {
            if (g.isIdle()) idleTime += g.getDuration();
            lastEnd = Math.max(lastEnd, g.getEndTime());
        }
        this.totalTime = lastEnd;
        this.cpuUtilization = lastEnd > 0 ? ((lastEnd - idleTime) / lastEnd) * 100.0 : 0;
    }

    public String getAlgorithmName() { return algorithmName; }
    public double getTimeQuantum() { return timeQuantum; }
    public List<SchedulingResult> getResults() { return results; }
    public List<GanttEntry> getGanttChart() { return ganttChart; }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getCpuUtilization() { return cpuUtilization; }
    public double getTotalTime() { return totalTime; }
    public int getProcessCount() { return results.size(); }
}
