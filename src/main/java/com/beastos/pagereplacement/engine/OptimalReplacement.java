package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;

import java.util.*;

/**
 * Optimal Page Replacement Algorithm.
 * Evicts the page that will not be used for the longest time in the future.
 * This is the theoretical best — used as a benchmark.
 */
public class OptimalReplacement implements PageReplacementAlgorithm {

    @Override
    public String getAlgorithmName() { return "Optimal"; }

    @Override
    public PageReplacementOutput simulate(int[] referenceString, int frameCount) {
        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);

        Set<Integer> inMemory = new HashSet<>();
        List<PageReplacementStep> steps = new ArrayList<>();

        for (int i = 0; i < referenceString.length; i++) {
            int page = referenceString[i];
            int replaced = -1;

            if (inMemory.contains(page)) {
                // HIT
                steps.add(new PageReplacementStep(i, page, frames, true, -1));
            } else {
                // FAULT
                if (inMemory.size() < frameCount) {
                    // Empty frame available
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == -1) {
                            frames[f] = page;
                            break;
                        }
                    }
                } else {
                    // Find the page used farthest in the future (or never used again)
                    int victimPage = -1;
                    int farthestUse = -1;

                    for (int p : inMemory) {
                        int nextUse = findNextUse(referenceString, p, i + 1);
                        if (nextUse == -1) {
                            // Never used again — optimal to evict
                            victimPage = p;
                            break;
                        }
                        if (nextUse > farthestUse) {
                            farthestUse = nextUse;
                            victimPage = p;
                        }
                    }

                    replaced = victimPage;
                    inMemory.remove(victimPage);

                    // Replace in frames
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == victimPage) {
                            frames[f] = page;
                            break;
                        }
                    }
                }

                inMemory.add(page);
                steps.add(new PageReplacementStep(i, page, frames, false, replaced));
            }
        }

        return new PageReplacementOutput(getAlgorithmName(), frameCount, referenceString, steps);
    }

    /**
     * Find the next occurrence of a page in the reference string after startIndex.
     * Returns -1 if not found (never used again).
     */
    private int findNextUse(int[] referenceString, int page, int startIndex) {
        for (int i = startIndex; i < referenceString.length; i++) {
            if (referenceString[i] == page) return i;
        }
        return -1;
    }
}
