package com.beastos.common;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the global simulation session state for the BEAST OS dashboard.
 * All dashboard statistics are derived from this session data.
 *
 * Singleton — one session per application lifecycle.
 */
public class SimulationSession {

    private static final SimulationSession INSTANCE = new SimulationSession();

    private final IntegerProperty activeProcesses = new SimpleIntegerProperty(0);
    private final DoubleProperty cpuUtilization = new SimpleDoubleProperty(0.0);
    private final DoubleProperty memoryInUse = new SimpleDoubleProperty(0.0);
    private final DoubleProperty totalMemory = new SimpleDoubleProperty(8.0); // GB, modeled
    private final IntegerProperty simulationsCompleted = new SimpleIntegerProperty(0);
    private final DoubleProperty resourceUtilization = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<SimulationState> simulationState =
            new SimpleObjectProperty<>(SimulationState.IDLE);

    // Last CPU scheduling result for dashboard Gantt
    private final ObjectProperty<Object> lastCpuResult = new SimpleObjectProperty<>(null);

    // Live list of active process names (e.g. "P1", "P2", "P3", ...)
    private final ObservableList<String> activeProcessNames = FXCollections.observableArrayList();

    private SimulationSession() {}

    public static SimulationSession getInstance() {
        return INSTANCE;
    }

    // --- Active Processes ---
    public int getActiveProcesses() { return activeProcesses.get(); }
    public IntegerProperty activeProcessesProperty() { return activeProcesses; }
    public void setActiveProcesses(int value) {
        activeProcesses.set(value);
        if (activeProcessNames.size() != value) {
            List<String> list = new ArrayList<>();
            for (int i = 1; i <= value; i++) {
                list.add("P" + i);
            }
            activeProcessNames.setAll(list);
        }
    }

    public ObservableList<String> getActiveProcessNames() {
        return activeProcessNames;
    }

    public void initDefaultProcesses(List<String> defaultNames) {
        activeProcessNames.setAll(defaultNames);
        activeProcesses.set(activeProcessNames.size());
    }

    /**
     * Terminate / kill a running process by PID live.
     * Updates active processes count, CPU, RAM, and triggers live UI refresh.
     */
    public void killProcess(String pid) {
        boolean removed = activeProcessNames.removeIf(p -> p.equalsIgnoreCase(pid));
        if (removed || activeProcesses.get() > 0) {
            int newCount = activeProcessNames.size();
            activeProcesses.set(newCount);

            if (newCount == 0) {
                cpuUtilization.set(4.0); // Baseline idle kernel load
                memoryInUse.set(1.10);   // Baseline kernel OS RAM
                resourceUtilization.set(5.0);
                simulationState.set(SimulationState.IDLE);
            } else {
                double dynamicCpu = Math.min(99.0, Math.max(12.0, newCount * 11.4 + 4.0));
                cpuUtilization.set(Math.round(dynamicCpu * 10.0) / 10.0);

                double dynamicMem = Math.min(totalMemory.get(), Math.max(1.10, newCount * 0.64 + 1.10));
                memoryInUse.set(Math.round(dynamicMem * 100.0) / 100.0);

                double dynamicRes = Math.min(95.0, Math.max(8.0, newCount * 6.0 + 2.0));
                resourceUtilization.set(Math.round(dynamicRes * 10.0) / 10.0);

                simulationState.set(SimulationState.RUNNING);
            }
            incrementSimulationsCompleted();
        }
    }

    /**
     * Spawn a new process live.
     * Generates a new unique PID and refreshes metrics live.
     */
    public void spawnProcess() {
        int nextNum = 1;
        while (true) {
            String candidate = "P" + nextNum;
            boolean exists = false;
            for (String p : activeProcessNames) {
                if (p.equalsIgnoreCase(candidate)) { exists = true; break; }
            }
            if (!exists) {
                activeProcessNames.add(candidate);
                break;
            }
            nextNum++;
        }
        int newCount = activeProcessNames.size();
        activeProcesses.set(newCount);

        double dynamicCpu = Math.min(99.0, Math.max(12.0, newCount * 11.4 + 4.0));
        cpuUtilization.set(Math.round(dynamicCpu * 10.0) / 10.0);

        double dynamicMem = Math.min(totalMemory.get(), Math.max(1.10, newCount * 0.64 + 1.10));
        memoryInUse.set(Math.round(dynamicMem * 100.0) / 100.0);

        double dynamicRes = Math.min(95.0, Math.max(8.0, newCount * 6.0 + 2.0));
        resourceUtilization.set(Math.round(dynamicRes * 10.0) / 10.0);

        simulationState.set(SimulationState.RUNNING);
    }

    // --- CPU Utilization ---
    public double getCpuUtilization() { return cpuUtilization.get(); }
    public DoubleProperty cpuUtilizationProperty() { return cpuUtilization; }
    public void setCpuUtilization(double value) { cpuUtilization.set(value); }

    // --- Memory In Use ---
    public double getMemoryInUse() { return memoryInUse.get(); }
    public DoubleProperty memoryInUseProperty() { return memoryInUse; }
    public void setMemoryInUse(double value) { memoryInUse.set(value); }

    // --- Total Memory ---
    public double getTotalMemory() { return totalMemory.get(); }
    public DoubleProperty totalMemoryProperty() { return totalMemory; }
    public void setTotalMemory(double value) { totalMemory.set(value); }

    // --- Simulations Completed ---
    public int getSimulationsCompleted() { return simulationsCompleted.get(); }
    public IntegerProperty simulationsCompletedProperty() { return simulationsCompleted; }
    public void setSimulationsCompleted(int value) { simulationsCompleted.set(value); }
    public void incrementSimulationsCompleted() {
        simulationsCompleted.set(simulationsCompleted.get() + 1);
    }

    // --- Simulation State ---
    public SimulationState getSimulationState() { return simulationState.get(); }
    public ObjectProperty<SimulationState> simulationStateProperty() { return simulationState; }
    public void setSimulationState(SimulationState state) { simulationState.set(state); }

    // --- Resource Utilization ---
    public double getResourceUtilization() { return resourceUtilization.get(); }
    public DoubleProperty resourceUtilizationProperty() { return resourceUtilization; }
    public void setResourceUtilization(double value) { resourceUtilization.set(value); }

    // --- Last CPU Result ---
    public Object getLastCpuResult() { return lastCpuResult.get(); }
    public ObjectProperty<Object> lastCpuResultProperty() { return lastCpuResult; }
    public void setLastCpuResult(Object result) { lastCpuResult.set(result); }

    /** Memory utilization as percentage. */
    public double getMemoryPercent() {
        if (totalMemory.get() <= 0) return 0;
        return (memoryInUse.get() / totalMemory.get()) * 100.0;
    }

    /** Reset all session data. */
    public void reset() {
        activeProcessNames.clear();
        activeProcesses.set(0);
        cpuUtilization.set(0);
        memoryInUse.set(0);
        simulationsCompleted.set(0);
        resourceUtilization.set(0);
        simulationState.set(SimulationState.IDLE);
        lastCpuResult.set(null);
    }
}
