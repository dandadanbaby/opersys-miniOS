package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;

import java.util.*;

/**
 * FIFO (First-In, First-Out) Page Replacement Algorithm.
 * Evicts the page that has been in memory the longest.
 */
public class FifoReplacement implements PageReplacementAlgorithm {

    @Override
    public String getAlgorithmName() { return "FIFO"; }

    @Override
    public PageReplacementOutput simulate(int[] referenceString, int frameCount) {
        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);

        Queue<Integer> queue = new LinkedList<>(); // tracks insertion order (page values)
        Set<Integer> inMemory = new HashSet<>();
        List<PageReplacementStep> steps = new ArrayList<>();

        for (int i = 0; i < referenceString.length; i++) {
            int page = referenceString[i];
            int replaced = -1;

            if (inMemory.contains(page)) {
                // HIT
                steps.add(new PageReplacementStep(i, page, frames, true, -1));
            } else {
                // FAULT — need to load this page
                if (inMemory.size() < frameCount) {
                    // Empty frame available — find first empty slot
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == -1) {
                            frames[f] = page;
                            break;
                        }
                    }
                } else {
                    // Evict oldest (front of queue)
                    int victim = queue.poll();
                    replaced = victim;
                    inMemory.remove(victim);

                    // Replace victim in frames
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == victim) {
                            frames[f] = page;
                            break;
                        }
                    }
                }
                queue.add(page);
                inMemory.add(page);
                steps.add(new PageReplacementStep(i, page, frames, false, replaced));
            }
        }

        return new PageReplacementOutput(getAlgorithmName(), frameCount, referenceString, steps);
    }
}
