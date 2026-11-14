package com.beastos.common;

/**
 * Represents the simulation status of the BEAST OS application.
 */
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
