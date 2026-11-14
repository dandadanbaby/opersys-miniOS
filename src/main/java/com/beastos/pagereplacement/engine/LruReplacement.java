package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;

import java.util.*;

/**
 * LRU (Least Recently Used) Page Replacement Algorithm.
 * Evicts the page that has not been used for the longest time.
 */
public class LruReplacement implements PageReplacementAlgorithm {

    @Override
    public String getAlgorithmName() { return "LRU"; }

    @Override
    public PageReplacementOutput simulate(int[] referenceString, int frameCount) {
        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);

        // Track last-used time for each page currently in memory
        Map<Integer, Integer> lastUsed = new HashMap<>();
        Set<Integer> inMemory = new HashSet<>();
        List<PageReplacementStep> steps = new ArrayList<>();

        for (int i = 0; i < referenceString.length; i++) {
            int page = referenceString[i];
            int replaced = -1;

            if (inMemory.contains(page)) {
                // HIT — update last used
                lastUsed.put(page, i);
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
                    // Find LRU page — the one with smallest lastUsed value
                    int lruPage = -1;
                    int lruTime = Integer.MAX_VALUE;
                    for (int p : inMemory) {
                        int t = lastUsed.getOrDefault(p, -1);
                        if (t < lruTime) {
                            lruTime = t;
                            lruPage = p;
                        }
                    }

                    replaced = lruPage;
                    inMemory.remove(lruPage);
                    lastUsed.remove(lruPage);

                    // Replace in frames
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == lruPage) {
                            frames[f] = page;
                            break;
                        }
                    }
                }

                inMemory.add(page);
                lastUsed.put(page, i);
                steps.add(new PageReplacementStep(i, page, frames, false, replaced));
            }
        }

        return new PageReplacementOutput(getAlgorithmName(), frameCount, referenceString, steps);
    }
}
