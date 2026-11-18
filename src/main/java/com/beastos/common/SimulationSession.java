package com.beastos.common;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/*
 * We made this class a singleton so our entire app shares the exact same
 * simulation state. Basically, all the live dashboard stats (like CPU load,
 * RAM in use, and active processes) pull from here so everything stays in sync across tabs.
 */
public class SimulationSession {

    private static final SimulationSession INSTANCE = new SimulationSession();

    private final IntegerProperty activeProcesses = new SimpleIntegerProperty(0);
    private final DoubleProperty cpuUtilization = new SimpleDoubleProperty(0.0);
    private final DoubleProperty memoryInUse = new SimpleDoubleProperty(0.0);
    private final DoubleProperty totalMemory = new SimpleDoubleProperty(8.0); // we're modeling a system with 8 GB of total RAM
    private final IntegerProperty simulationsCompleted = new SimpleIntegerProperty(0);
    private final DoubleProperty resourceUtilization = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<SimulationState> simulationState =
            new SimpleObjectProperty<>(SimulationState.IDLE);

    // we keep the latest CPU scheduling run here so our main dashboard can draw its Gantt chart
    private final ObjectProperty<Object> lastCpuResult = new SimpleObjectProperty<>(null);

    // live list of running process names (like "P1", "P2") — using ObservableList so the JavaFX UI updates automatically
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
        int count = activeProcessNames.size();
        activeProcesses.set(count);

        // we scale the initial metrics proportionally based on how many processes are loaded
        if (count > 0) {
            double dynamicCpu = Math.min(99.0, Math.max(12.0, count * 11.4 + 4.0));
            cpuUtilization.set(Math.round(dynamicCpu * 10.0) / 10.0);

            double dynamicMem = Math.min(totalMemory.get(), Math.max(1.10, count * 0.64 + 1.10));
            memoryInUse.set(Math.round(dynamicMem * 100.0) / 100.0);

            double dynamicRes = Math.min(95.0, Math.max(8.0, count * 6.0 + 2.0));
            resourceUtilization.set(Math.round(dynamicRes * 10.0) / 10.0);

            simulationState.set(SimulationState.RUNNING);
        }
    }

    /*
     * This is where we kill a running process by its PID live!
     * We drop it from our list and recalculate CPU and RAM usage right away
     * so the dashboard reflects the change immediately.
     */
    public void killProcess(String pid) {
        boolean removed = activeProcessNames.removeIf(p -> p.equalsIgnoreCase(pid));
        if (removed || activeProcesses.get() > 0) {
            int newCount = activeProcessNames.size();
            activeProcesses.set(newCount);

            if (newCount == 0) {
                cpuUtilization.set(4.0); // baseline idle CPU load just for our mini kernel background tasks
                memoryInUse.set(1.10);   // baseline kernel OS RAM in GB
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

    /*
     * Spawns a new process live!
     * We loop to find the next free PID number (like "P1", "P2"...) so we don't get duplicate IDs,
     * add it to our active list, and bump up the simulated system load.
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

    // quick helper we wrote to calculate memory usage as a percentage (used / total * 100)
    public double getMemoryPercent() {
        if (totalMemory.get() <= 0) return 0;
        return (memoryInUse.get() / totalMemory.get()) * 100.0;
    }

    // resets all our session counters and metrics back to startup defaults
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
