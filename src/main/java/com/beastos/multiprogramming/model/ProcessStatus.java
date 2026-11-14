package com.beastos.multiprogramming.model;

/**
 * Status of a process in the multiprogramming environment.
 */
public enum ProcessStatus {
    ACTIVE("Active in Memory"),
    WAITING("Waiting (RAM Full)"),
    TERMINATED("Terminated");

    private final String displayName;

    ProcessStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
