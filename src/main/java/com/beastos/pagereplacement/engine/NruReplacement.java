package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;

import java.util.*;

/**
 * NRU (Not Recently Used) Page Replacement Algorithm.
 *
 * How it works:
 * - Each page in memory maintains two status bits:
 *   - R (Referenced bit): set to 1 when a page is read or referenced.
 *   - M (Modified bit): set to 1 when a page is modified (written to).
 * - Pages are partitioned into four priority classes:
 *   - Class 0: R = 0, M = 0 (Not recently referenced, not modified — optimal to evict)
 *   - Class 1: R = 0, M = 1 (Not recently referenced, but modified — must be written back)
 *   - Class 2: R = 1, M = 0 (Recently referenced, not modified — clean working set page)
 *   - Class 3: R = 1, M = 1 (Recently referenced and modified — active dirty page)
 * - Simulation details:
 *   - When loaded into memory, R = 1, M = 0.
 *   - On a cache hit (repeated access), R = 1, M = 1 (simulates write / dirtying in memory).
 *   - On periodic clock tick (every 4 references), all R bits in memory are cleared to 0
 *     so the system can distinguish recently accessed pages from idle ones.
 *   - On a page fault when frames are full:
 *     - The algorithm inspects all resident pages and identifies the lowest non-empty class (0 -> 1 -> 2 -> 3).
 *     - If multiple pages reside in that lowest class, the earliest loaded page (FIFO tie-break) is evicted.
 */
public class NruReplacement implements PageReplacementAlgorithm {

    // Periodic interval (number of references) after which all R bits are cleared to 0
    private static final int CLOCK_RESET_INTERVAL = 4;

    @Override
    public String getAlgorithmName() {
        return "NRU";
    }

    @Override
    public PageReplacementOutput simulate(int[] referenceString, int frameCount) {
        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);

        Map<Integer, Boolean> rBits = new HashMap<>();
        Map<Integer, Boolean> mBits = new HashMap<>();
        Map<Integer, Integer> loadTime = new HashMap<>();

        Set<Integer> inMemory = new HashSet<>();
        List<PageReplacementStep> steps = new ArrayList<>();

        for (int i = 0; i < referenceString.length; i++) {
            // Periodic clock interrupt: clear R bits every CLOCK_RESET_INTERVAL steps
            if (i > 0 && i % CLOCK_RESET_INTERVAL == 0) {
                for (int p : inMemory) {
                    rBits.put(p, false);
                }
            }

            int page = referenceString[i];
            int replaced = -1;

            if (inMemory.contains(page)) {
                // HIT: page is referenced and modified
                rBits.put(page, true);
                mBits.put(page, true);
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
                    // All frames occupied — find victim from lowest non-empty class
                    int victim = -1;
                    int lowestClass = 5;
                    int earliestLoad = Integer.MAX_VALUE;

                    for (int p : inMemory) {
                        boolean r = rBits.getOrDefault(p, false);
                        boolean m = mBits.getOrDefault(p, false);
                        int pClass;

                        if (!r && !m) pClass = 0;
                        else if (!r && m) pClass = 1;
                        else if (r && !m) pClass = 2;
                        else pClass = 3;

                        int t = loadTime.getOrDefault(p, 0);

                        if (pClass < lowestClass || (pClass == lowestClass && t < earliestLoad)) {
                            lowestClass = pClass;
                            earliestLoad = t;
                            victim = p;
                        }
                    }

                    replaced = victim;
                    inMemory.remove(victim);
                    rBits.remove(victim);
                    mBits.remove(victim);
                    loadTime.remove(victim);

                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == victim) {
                            frames[f] = page;
                            break;
                        }
                    }
                }

                inMemory.add(page);
                rBits.put(page, true);
                mBits.put(page, false);
                loadTime.put(page, i);
                steps.add(new PageReplacementStep(i, page, frames, false, replaced));
            }
        }

        return new PageReplacementOutput(getAlgorithmName(), frameCount, referenceString, steps);
    }
}
