package com.beastos.multiprogramming.engine;

import com.beastos.multiprogramming.model.MultiprogrammingOutput;
import com.beastos.multiprogramming.model.ProcessStatus;
import com.beastos.multiprogramming.model.ProgramProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiprogramming simulation engine.
 * Simulates process loading, memory allocation into a shared RAM pool,
 * job queuing (waiting when RAM full), and dynamic resource utilization.
 *
 * Pure Java — zero JavaFX dependencies.
 */
public class MultiprogrammingEngine {

    /**
     * Allocate memory for a list of processes in FIFO arrival order.
     *
     * @param totalRamMb total physical RAM available in megabytes
     * @param requests   processes requesting memory
     * @return simulation output with allocation status and system resource metrics
     */
    public MultiprogrammingOutput allocate(int totalRamMb, List<ProgramProcess> requests) {
        if (totalRamMb <= 0 || requests == null || requests.isEmpty()) {
            return new MultiprogrammingOutput(Math.max(0, totalRamMb), 0, 0.0, 0.0, List.of());
        }

        List<ProgramProcess> processed = new ArrayList<>();
        int usedRam = 0;

        for (ProgramProcess req : requests) {
            int mem = req.getMemoryNeeded();
            if (usedRam + mem <= totalRamMb) {
                int base = usedRam;
                usedRam += mem;
                int remaining = totalRamMb - usedRam;
                processed.add(new ProgramProcess(req.getPid(), mem, ProcessStatus.ACTIVE, remaining, base));
            } else {
                int remaining = totalRamMb - usedRam;
                processed.add(new ProgramProcess(req.getPid(), mem, ProcessStatus.WAITING, remaining, -1));
            }
        }

        double memUtil = (usedRam * 100.0) / totalRamMb;
        int activeCount = (int) processed.stream().filter(ProgramProcess::isActive).count();

        // Realistic OS resource load formulas (calibrated to match design reference)
        double cpuLoad = calculateCpuLoad(activeCount, memUtil);
        double resourceLoad = calculateResourceLoad(activeCount);

        return new MultiprogrammingOutput(totalRamMb, usedRam, cpuLoad, resourceLoad, processed);
    }

    /**
     * Terminate an active process by PID, free its memory, and admit waiting processes
     * that now fit into the newly freed RAM.
     *
     * @param totalRamMb       total physical RAM
     * @param currentProcesses current state of all processes
     * @param pidToTerminate   PID of the process to terminate
     * @return new simulation output after termination and re-allocation
     */
    public MultiprogrammingOutput terminateAndCompact(int totalRamMb, List<ProgramProcess> currentProcesses, String pidToTerminate) {
        if (currentProcesses == null || currentProcesses.isEmpty()) {
            return allocate(totalRamMb, List.of());
        }

        List<ProgramProcess> updatedList = new ArrayList<>();
        for (ProgramProcess p : currentProcesses) {
            if (p.getPid().equalsIgnoreCase(pidToTerminate)) {
                // Skip the terminated process to remove it from memory
                continue;
            }
            // Reset to waiting/clean state for re-allocation
            updatedList.add(new ProgramProcess(p.getPid(), p.getMemoryNeeded()));
        }

        return allocate(totalRamMb, updatedList);
    }

    /**
     * Input validation.
     */
    public List<String> validate(int totalRamMb, List<Integer> processSizes) {
        List<String> errors = new ArrayList<>();
        if (totalRamMb <= 0) {
            errors.add("Total System RAM must be greater than 0 MB.");
        }
        if (processSizes == null || processSizes.isEmpty()) {
            errors.add("At least one process memory requirement must be provided.");
        } else {
            for (int i = 0; i < processSizes.size(); i++) {
                int size = processSizes.get(i);
                if (size <= 0) {
                    errors.add("Process P" + (i + 1) + " memory request must be positive.");
                }
            }
        }
        return errors;
    }

    private double calculateCpuLoad(int activeCount, double memUtil) {
        if (activeCount == 0) return 0.0;
        // Calibrated: For 6 active processes with 96.875% RAM load -> 72.4%
        double cpu = 15.0 + (activeCount * 9.0) + (memUtil * 0.035);
        return Math.min(99.9, Math.round(cpu * 10.0) / 10.0);
    }

    private double calculateResourceLoad(int activeCount) {
        if (activeCount == 0) return 0.0;
        // Calibrated: For 6 active processes -> 38.0%
        double res = 8.0 + (activeCount * 5.0);
        return Math.min(99.9, Math.round(res * 10.0) / 10.0);
    }
}
