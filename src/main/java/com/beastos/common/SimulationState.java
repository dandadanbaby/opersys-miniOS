package com.beastos.common;

// we created this enum to keep track of what our simulation is currently doing.
// basically, it tells the UI whether we're waiting for inputs (IDLE), set up and good to go (READY),
// actively running the algorithms (RUNNING), paused mid-step (PAUSED), or done with everything (COMPLETED)
public enum SimulationState {
    IDLE("IDLE"),
    READY("READY"),
    RUNNING("RUNNING"),
    PAUSED("PAUSED"),
    COMPLETED("COMPLETED");

    private final String displayName;

    SimulationState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
