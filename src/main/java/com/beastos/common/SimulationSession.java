package com.beastos.common;

import javafx.beans.property.*;

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

    private SimulationSession() {}

    public static SimulationSession getInstance() {
        return INSTANCE;
    }

    // --- Active Processes ---
    public int getActiveProcesses() { return activeProcesses.get(); }
    public IntegerProperty activeProcessesProperty() { return activeProcesses; }
    public void setActiveProcesses(int value) { activeProcesses.set(value); }

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
        activeProcesses.set(0);
        cpuUtilization.set(0);
        memoryInUse.set(0);
        simulationsCompleted.set(0);
        resourceUtilization.set(0);
        simulationState.set(SimulationState.IDLE);
        lastCpuResult.set(null);
    }
}
