package com.beastos.cpu.engine;

import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;

import java.util.List;

/**
 * Interface for all CPU scheduling algorithms.
 * Implementations must be pure Java — no JavaFX dependency.
 */
public interface CpuScheduler {

    /**
     * Execute the scheduling algorithm on the given processes.
     *
     * @param processes list of processes (must not be empty)
     * @return complete scheduling output with results and Gantt chart
     */
    SchedulingOutput schedule(List<Process> processes);

    /** Return the display name of this algorithm. */
    String getAlgorithmName();
}
