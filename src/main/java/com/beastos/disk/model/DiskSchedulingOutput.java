package com.beastos.disk.model;

import java.util.List;

/**
 * Complete output of a disk scheduling simulation.
 */
public class DiskSchedulingOutput {
    private final String algorithmName;
    private final int initialPosition;
    private final int cylinderLimit;
    private final List<Integer> seekSequence;    // ordered list of cylinders visited
    private final List<SeekStep> steps;
    private final int totalSeekDistance;
    private final double averageSeekDistance;

    public DiskSchedulingOutput(String algorithmName, int initialPosition, int cylinderLimit,
                                 List<Integer> seekSequence, List<SeekStep> steps) {
        this.algorithmName = algorithmName;
        this.initialPosition = initialPosition;
        this.cylinderLimit = cylinderLimit;
        this.seekSequence = List.copyOf(seekSequence);
        this.steps = List.copyOf(steps);

        int total = 0;
        for (SeekStep s : steps) total += s.getSeekDistance();
        this.totalSeekDistance = total;
        this.averageSeekDistance = steps.isEmpty() ? 0.0 : (double) total / steps.size();
    }

    public String getAlgorithmName() { return algorithmName; }
    public int getInitialPosition() { return initialPosition; }
    public int getCylinderLimit() { return cylinderLimit; }
    public List<Integer> getSeekSequence() { return seekSequence; }
    public List<SeekStep> getSteps() { return steps; }
    public int getTotalSeekDistance() { return totalSeekDistance; }
    public double getAverageSeekDistance() { return averageSeekDistance; }
}
