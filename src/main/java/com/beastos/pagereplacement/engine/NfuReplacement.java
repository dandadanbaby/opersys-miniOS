package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;

import java.util.*;

/**
 * NFU (Not Frequently Used) Page Replacement Algorithm.
 *
 * How it works:
 * - Each page currently resident in memory maintains a software reference counter.
 * - When a page is referenced (either on initial load or on a cache hit), its
 *   counter is incremented by 1.
 * - When a page fault occurs and all frames are full:
 *   - The algorithm searches for the page with the lowest counter value (least frequently accessed).
 *   - If there is a tie between multiple pages with the same minimum frequency,
 *     the tie is broken by FIFO (the page that entered memory earliest).
 *   - The victim page is evicted and its counter is discarded.
 *   - The new incoming page is loaded into the vacated frame with an initial count of 1.
 */
public class NfuReplacement implements PageReplacementAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "NFU";
    }

    @Override
    public PageReplacementOutput simulate(int[] referenceString, int frameCount) {
        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);

        // Tracks frequency count for each page currently in memory
        Map<Integer, Integer> freqCount = new HashMap<>();
        // Tracks insertion time step (for FIFO tie-breaking)
        Map<Integer, Integer> loadTime = new HashMap<>();

        Set<Integer> inMemory = new HashSet<>();
        List<PageReplacementStep> steps = new ArrayList<>();

        for (int i = 0; i < referenceString.length; i++) {
            int page = referenceString[i];
            int replaced = -1;

            if (inMemory.contains(page)) {
                // HIT: increment frequency counter
                freqCount.put(page, freqCount.getOrDefault(page, 0) + 1);
                steps.add(new PageReplacementStep(i, page, frames, true, -1));
            } else {
                // FAULT
                if (inMemory.size() < frameCount) {
                    // Empty frame available — place in first free slot
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == -1) {
                            frames[f] = page;
                            break;
                        }
                    }
                } else {
                    // Evict page with minimum frequency; tie-break with earliest load time
                    int victim = -1;
                    int minFreq = Integer.MAX_VALUE;
                    int earliestLoad = Integer.MAX_VALUE;

                    for (int p : inMemory) {
                        int f = freqCount.getOrDefault(p, 0);
                        int t = loadTime.getOrDefault(p, 0);

                        if (f < minFreq || (f == minFreq && t < earliestLoad)) {
                            minFreq = f;
                            earliestLoad = t;
                            victim = p;
                        }
                    }

                    replaced = victim;
                    inMemory.remove(victim);
                    freqCount.remove(victim);
                    loadTime.remove(victim);

                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == victim) {
                            frames[f] = page;
                            break;
                        }
                    }
                }

                inMemory.add(page);
                freqCount.put(page, 1);
                loadTime.put(page, i);
                steps.add(new PageReplacementStep(i, page, frames, false, replaced));
            }
        }

        return new PageReplacementOutput(getAlgorithmName(), frameCount, referenceString, steps);
    }
}
