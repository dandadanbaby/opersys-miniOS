package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;

/**
 * Interface for all page replacement algorithms.
 * Implementations must be pure Java — no JavaFX dependency.
 */
public interface PageReplacementAlgorithm {

    /**
     * Simulate the page replacement algorithm.
     *
     * @param referenceString the sequence of page references
     * @param frameCount      number of available frames (must be > 0)
     * @return complete simulation output with step-by-step results
     */
    PageReplacementOutput simulate(int[] referenceString, int frameCount);

    /** Return the display name of this algorithm. */
    String getAlgorithmName();
}
