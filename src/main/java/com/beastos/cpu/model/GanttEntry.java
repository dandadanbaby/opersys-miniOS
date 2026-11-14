package com.beastos.cpu.model;

/**
 * A single entry in the Gantt chart timeline.
 * Represents one continuous execution block.
 */
public class GanttEntry {
    private final String pid;  // process ID, or "IDLE" for CPU idle
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
