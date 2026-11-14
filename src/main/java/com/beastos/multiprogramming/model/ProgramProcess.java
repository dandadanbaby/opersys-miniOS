package com.beastos.multiprogramming.model;

/**
 * Represents a process requesting memory in a multiprogramming system.
 */
public class ProgramProcess {
    private final String pid;
    private final int memoryNeeded; // in MB
    private ProcessStatus status;
    private int remainingRam;       // in MB after allocation step
    private int baseAddress;        // start MB in RAM (-1 if not active)

    public ProgramProcess(String pid, int memoryNeeded) {
        this(pid, memoryNeeded, ProcessStatus.WAITING, -1, -1);
    }

    public ProgramProcess(String pid, int memoryNeeded, ProcessStatus status, int remainingRam, int baseAddress) {
        this.pid = pid;
        this.memoryNeeded = memoryNeeded;
        this.status = status;
        this.remainingRam = remainingRam;
        this.baseAddress = baseAddress;
    }

    public String getPid() {
        return pid;
    }

    public int getMemoryNeeded() {
        return memoryNeeded;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public int getRemainingRam() {
        return remainingRam;
    }

    public void setRemainingRam(int remainingRam) {
        this.remainingRam = remainingRam;
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public boolean isActive() {
        return status == ProcessStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return pid + " (" + memoryNeeded + " MB) -> " + status;
    }
}
