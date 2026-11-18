package com.beastos.cpu.engine;

import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;

import java.util.List;

// this is the base interface that all our CPU scheduling algorithms have to implement (FCFS, SJF, RR, Priority)
// we made sure to keep this pure Java with no JavaFX imports so the simulation logic stays separate from the UI!
public interface CpuScheduler {

    // this is where we actually run the algorithm on the processes
    // basically it takes the process list and returns the Gantt chart plus all calculated times (wait, turnaround, etc.)
    SchedulingOutput schedule(List<Process> processes);

    // returns the readable name of the algorithm so we can show it in the UI dropdown and labels
    String getAlgorithmName();
}
