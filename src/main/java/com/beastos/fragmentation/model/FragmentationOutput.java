package com.beastos.fragmentation.model;

import java.util.List;

/**
 * Complete output of a fragmentation simulation.
 */
public class FragmentationOutput {
    private final String algorithmName;
    private final List<AllocationResult> results;
    private final int totalBlocks;
    private final int allocatedCount;
    private final int waitingCount;
    private final int totalInternalFrag;
    private final int externalFrag;

    public FragmentationOutput(String algorithmName, List<AllocationResult> results,
                                int[] originalBlockSizes) {
        this.algorithmName = algorithmName;
        this.results = List.copyOf(results);
        this.totalBlocks = originalBlockSizes.length;

        int allocated = 0, waiting = 0, intFrag = 0;
        boolean[] blockUsed = new boolean[originalBlockSizes.length];

        for (AllocationResult r : results) {
            if (r.isAllocated()) {
                allocated++;
                intFrag += r.getInternalFrag();
                if (r.getBlockId() > 0 && r.getBlockId() <= originalBlockSizes.length) {
                    blockUsed[r.getBlockId() - 1] = true;
                }
            } else {
                waiting++;
            }
        }

        // External fragmentation = sum of unused block sizes
        int extFrag = 0;
        for (int i = 0; i < originalBlockSizes.length; i++) {
            if (!blockUsed[i]) {
                extFrag += originalBlockSizes[i];
            }
        }

        this.allocatedCount = allocated;
        this.waitingCount = waiting;
        this.totalInternalFrag = intFrag;
        this.externalFrag = extFrag;
    }

    public String getAlgorithmName() { return algorithmName; }
    public List<AllocationResult> getResults() { return results; }
    public int getTotalBlocks() { return totalBlocks; }
    public int getAllocatedCount() { return allocatedCount; }
    public int getWaitingCount() { return waitingCount; }
    public int getTotalInternalFrag() { return totalInternalFrag; }
    public int getExternalFrag() { return externalFrag; }
    public int getTotalProcesses() { return results.size(); }
}
