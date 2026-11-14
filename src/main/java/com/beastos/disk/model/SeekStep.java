package com.beastos.disk.model;

/**
 * One step in the disk seek sequence showing the head moving from one cylinder to another.
 */
public class SeekStep {
    private final int stepNumber;
    private final String transitionLabel;  // e.g. "P1 → P2"
    private final int previousCylinder;
    private final int targetCylinder;
    private final int seekDistance;        // |target - previous|

    public SeekStep(int stepNumber, String transitionLabel,
                    int previousCylinder, int targetCylinder) {
        this.stepNumber = stepNumber;
        this.transitionLabel = transitionLabel;
        this.previousCylinder = previousCylinder;
        this.targetCylinder = targetCylinder;
        this.seekDistance = Math.abs(targetCylinder - previousCylinder);
    }

    public int getStepNumber() { return stepNumber; }
    public String getTransitionLabel() { return transitionLabel; }
    public int getPreviousCylinder() { return previousCylinder; }
    public int getTargetCylinder() { return targetCylinder; }
    public int getSeekDistance() { return seekDistance; }
}
