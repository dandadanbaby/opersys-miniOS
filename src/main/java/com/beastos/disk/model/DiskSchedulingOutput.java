package com.beastos.disk.model;

import java.util.List;

// this class holds all the results from a disk scheduling simulation run.
// basically, once an algorithm finishes, we bundle together the sequence of cylinders visited,
// the step-by-step breakdown, and the total/average seek distances so the UI can display everything easily.
public class DiskSchedulingOutput {
    private final String algorithmName;
    private final int initialPosition;
    private final int cylinderLimit;
    private final List<Integer> seekSequence;    // the order of cylinders the disk head actually visited
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

        // we sum up the seek distance from each step to find the total head movement
        int total = 0;
        for (SeekStep s : steps) total += s.getSeekDistance();
        this.totalSeekDistance = total;
        // basically calculating the mean seek distance here, watching out for divide-by-zero if there are no steps
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
