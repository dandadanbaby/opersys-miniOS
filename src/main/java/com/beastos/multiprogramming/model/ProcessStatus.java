package com.beastos.multiprogramming.model;

public enum ProcessStatus {
    RUNNING("Running"),
    COMPLETED("Completed"),
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
