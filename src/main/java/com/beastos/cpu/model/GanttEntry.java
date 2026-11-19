package com.beastos.cpu.model;

// this is where we represent a single block on our Gantt chart timeline.
// basically what happens here is whenever a process gets the CPU (or if the CPU is just sitting idle),
// we save who ran, when it started, and when it finished so we can draw each colored slice in the UI later!
public class GanttEntry {
    private final String pid; // process ID, or "IDLE" if the CPU was doing nothing
    private final double startTime;
    private final double endTime;

    public GanttEntry(String pid, double startTime, double endTime) {
        this.pid = pid;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getPid() { return pid; }
    public double getStartTime() { return startTime; }
    public double getEndTime() { return endTime; }
    public double getDuration() { return endTime - startTime; }
    public boolean isIdle() { return "IDLE".equals(pid); }

    @Override
    public String toString() {
        return "[" + pid + ": " + startTime + "-" + endTime + "]";
    }
}
