package com.beastos.multiprogramming.model;

import java.util.List;

/**
 * Output results from a multiprogramming simulation.
 */
public class MultiprogrammingOutput {
    private final int totalMemory;           // MB
    private final int allocatedMemory;       // MB
    private final int freeMemory;            // MB
    private final double memoryUtilization;  // percentage (0 - 100)
    private final double cpuLoad;            // simulated CPU load %
    private final double resourceLoad;       // simulated peripheral/resource load %
    private final int degreeOfMultiprogramming; // count of active processes
    private final int totalProcesses;
    private final int waitingCount;
    private final List<ProgramProcess> processes;

    public MultiprogrammingOutput(int totalMemory, int allocatedMemory,
                                  double cpuLoad, double resourceLoad,
                                  List<ProgramProcess> processes) {
        this.totalMemory = totalMemory;
        this.allocatedMemory = allocatedMemory;
        this.freeMemory = Math.max(0, totalMemory - allocatedMemory);
        this.memoryUtilization = totalMemory > 0 ? (allocatedMemory * 100.0) / totalMemory : 0.0;
        this.cpuLoad = cpuLoad;
        this.resourceLoad = resourceLoad;
        this.processes = List.copyOf(processes);

        int active = 0;
        int waiting = 0;
        for (ProgramProcess p : processes) {
            if (p.isActive()) {
                active++;
            } else if (p.getStatus() == ProcessStatus.WAITING) {
                waiting++;
            }
        }
        this.degreeOfMultiprogramming = active;
        this.totalProcesses = processes.size();
        this.waitingCount = waiting;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getAllocatedMemory() {
        return allocatedMemory;
    }

    public int getFreeMemory() {
        return freeMemory;
    }

    public double getMemoryUtilization() {
        return memoryUtilization;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getResourceLoad() {
        return resourceLoad;
    }

    public int getDegreeOfMultiprogramming() {
        return degreeOfMultiprogramming;
    }

    public int getTotalProcesses() {
        return totalProcesses;
    }

    public int getWaitingCount() {
        return waitingCount;
    }

    public List<ProgramProcess> getProcesses() {
        return processes;
    }

    public List<ProgramProcess> getActiveProcesses() {
        return processes.stream().filter(ProgramProcess::isActive).toList();
    }

    public List<ProgramProcess> getWaitingProcesses() {
        return processes.stream().filter(p -> p.getStatus() == ProcessStatus.WAITING).toList();
    }
}
