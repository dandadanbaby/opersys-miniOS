package com.beastos.pagereplacement.engine;

import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;

import java.util.*;

/**
 * Second Chance (Clock) Page Replacement Algorithm.
 *
 * How it works:
 * - Operates as a circular queue with a clock hand pointer.
 * - Each frame has a reference bit (initially 1 when loaded or referenced).
 * - On a page hit, the referenced frame's bit is set to 1.
 * - On a page fault when frames are full:
 *   - The clock hand inspects frames in circular order.
 *   - If the current frame has reference bit = 1, it is cleared to 0 ("second chance"),
 *     and the clock hand advances to the next frame.
 *   - If the current frame has reference bit = 0, this page is selected as the victim,
 *     evicted, replaced with the new page (bit set to 1), and the clock hand advances.
 */
public class SecondChanceReplacement implements PageReplacementAlgorithm {

    @Override
    public String getAlgorithmName() {
        return "Second Chance";
    }

    @Override
    public PageReplacementOutput simulate(int[] referenceString, int frameCount) {
        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);
        boolean[] refBits = new boolean[frameCount];
        int pointer = 0; // clock hand pointer

        Set<Integer> inMemory = new HashSet<>();
        List<PageReplacementStep> steps = new ArrayList<>();

        for (int i = 0; i < referenceString.length; i++) {
            int page = referenceString[i];
            int replaced = -1;

            if (inMemory.contains(page)) {
                // HIT: set reference bit for this page's frame to true
                for (int f = 0; f < frameCount; f++) {
                    if (frames[f] == page) {
                        refBits[f] = true;
                        break;
                    }
                }
                steps.add(new PageReplacementStep(i, page, frames, true, -1));
            } else {
                // FAULT
                if (inMemory.size() < frameCount) {
                    // Empty frame available — place in first empty slot
                    for (int f = 0; f < frameCount; f++) {
                        if (frames[f] == -1) {
                            frames[f] = page;
                            refBits[f] = true;
                            break;
                        }
                    }
                } else {
                    // All frames occupied — use circular clock hand to find victim
                    while (refBits[pointer]) {
                        // Second chance granted: clear bit to 0 and move clock hand
                        refBits[pointer] = false;
                        pointer = (pointer + 1) % frameCount;
                    }

                    // Found victim with reference bit 0
                    int victim = frames[pointer];
                    replaced = victim;
                    inMemory.remove(victim);

                    // Replace victim with new page
                    frames[pointer] = page;
                    refBits[pointer] = true;
                    pointer = (pointer + 1) % frameCount;
                }

                inMemory.add(page);
                steps.add(new PageReplacementStep(i, page, frames, false, replaced));
            }
        }

        return new PageReplacementOutput(getAlgorithmName(), frameCount, referenceString, steps);
    }
}
