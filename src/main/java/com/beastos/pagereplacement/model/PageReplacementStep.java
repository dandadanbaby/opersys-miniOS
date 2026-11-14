package com.beastos.pagereplacement.model;

import java.util.Arrays;

/**
 * Represents one step of a page replacement simulation.
 * Shows the page requested, frame state after this step, and hit/fault status.
 */
public class PageReplacementStep {
    private final int stepIndex;
    private final int page;
    private final int[] frameState;  // current state of all frames after this step (-1 = empty)
    private final boolean hit;
    private final int replacedPage;  // the page that was evicted, or -1 if no eviction

    public PageReplacementStep(int stepIndex, int page, int[] frameState,
                                boolean hit, int replacedPage) {
        this.stepIndex = stepIndex;
        this.page = page;
        this.frameState = Arrays.copyOf(frameState, frameState.length);
        this.hit = hit;
        this.replacedPage = replacedPage;
    }

    public int getStepIndex() { return stepIndex; }
    public int getPage() { return page; }
    public int[] getFrameState() { return Arrays.copyOf(frameState, frameState.length); }
    public boolean isHit() { return hit; }
    public boolean isFault() { return !hit; }
    public int getReplacedPage() { return replacedPage; }

    public String getStatusText() { return hit ? "HIT" : "FAULT"; }

    public String getFrameStateString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < frameState.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(frameState[i] == -1 ? "-1" : String.valueOf(frameState[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
