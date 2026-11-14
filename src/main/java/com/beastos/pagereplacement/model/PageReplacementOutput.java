package com.beastos.pagereplacement.model;

import java.util.List;

/**
 * Complete output of a page replacement simulation.
 */
public class PageReplacementOutput {
    private final String algorithmName;
    private final int frameCount;
    private final int[] referenceString;
    private final List<PageReplacementStep> steps;
    private final int totalRequests;
    private final int hitCount;
    private final int faultCount;
    private final double hitRatio;
    private final double faultRatio;

    public PageReplacementOutput(String algorithmName, int frameCount, int[] referenceString,
                                  List<PageReplacementStep> steps) {
        this.algorithmName = algorithmName;
        this.frameCount = frameCount;
        this.referenceString = referenceString.clone();
        this.steps = List.copyOf(steps);
        this.totalRequests = steps.size();

        int hits = 0;
        for (PageReplacementStep step : steps) {
            if (step.isHit()) hits++;
        }
        this.hitCount = hits;
        this.faultCount = totalRequests - hits;
        this.hitRatio = totalRequests > 0 ? (double) hits / totalRequests * 100.0 : 0;
        this.faultRatio = totalRequests > 0 ? (double) faultCount / totalRequests * 100.0 : 0;
    }

    public String getAlgorithmName() { return algorithmName; }
    public int getFrameCount() { return frameCount; }
    public int[] getReferenceString() { return referenceString.clone(); }
    public List<PageReplacementStep> getSteps() { return steps; }
    public int getTotalRequests() { return totalRequests; }
    public int getHitCount() { return hitCount; }
    public int getFaultCount() { return faultCount; }
    public double getHitRatio() { return hitRatio; }
    public double getFaultRatio() { return faultRatio; }
}
