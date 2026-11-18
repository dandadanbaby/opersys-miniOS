package com.beastos.cpu.model;

// this is where we represent a single block on our Gantt chart timeline.
// basically what happens here is whenever a process gets the CPU (or if the CPU is just sitting idle),
// we save who ran, when it started, and when it finished so we can draw each colored slice in the UI later!
public class GanttEntry {
    private final String pid; // process ID, or "IDLE" if the CPU was doing nothing
    private final int startTime;
    private final int endTime;

    public GanttEntry(String pid, int startTime, int endTime) {
        this.pid = pid;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getPid() { return pid; }
    public int getStartTime() { return startTime; }
    public int getEndTime() { return endTime; }
    public int getDuration() { return endTime - startTime; }
    public boolean isIdle() { return "IDLE".equals(pid); }

    @Override
    public String toString() {
        return "[" + pid + ": " + startTime + "-" + endTime + "]";
    }
}
