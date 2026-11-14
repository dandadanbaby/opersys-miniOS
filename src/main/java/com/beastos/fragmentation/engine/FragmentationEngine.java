package com.beastos.fragmentation.engine;

import com.beastos.fragmentation.model.AllocationResult;
import com.beastos.fragmentation.model.FragmentationOutput;
import com.beastos.fragmentation.model.MemoryBlock;

import java.util.*;

/**
 * Memory allocation engine supporting First-Fit, Best-Fit, and Worst-Fit strategies.
 * Fixed-partition allocation: each process gets one block, internal fragmentation tracked.
 *
 * Pure Java — no JavaFX dependency.
 */
public class FragmentationEngine {

    public enum Strategy { FIRST_FIT, BEST_FIT, WORST_FIT }

    /**
     * Allocate processes to memory blocks using the given strategy.
     *
     * @param blocks    available memory blocks (each used at most once)
     * @param processes process entries: [processId, processSize]
     * @param strategy  allocation strategy
     * @return complete allocation output
     */
    public FragmentationOutput allocate(List<MemoryBlock> blocks, String[] processIds,
                                         int[] processSizes, Strategy strategy) {
        // Track which blocks are still available
        boolean[] blockUsed = new boolean[blocks.size()];
        List<AllocationResult> results = new ArrayList<>();

        int[] originalSizes = new int[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            originalSizes[i] = blocks.get(i).getSize();
        }

        for (int i = 0; i < processIds.length; i++) {
            String pid = processIds[i];
            int pSize = processSizes[i];

            int selectedBlock = findBlock(blocks, blockUsed, pSize, strategy);

            if (selectedBlock >= 0) {
                blockUsed[selectedBlock] = true;
                MemoryBlock b = blocks.get(selectedBlock);
                results.add(new AllocationResult(pid, pSize, b.getBlockId(), b.getSize(), true));
            } else {
                results.add(new AllocationResult(pid, pSize, -1, 0, false));
            }
        }

        return new FragmentationOutput(strategyName(strategy), results, originalSizes);
    }

    private int findBlock(List<MemoryBlock> blocks, boolean[] used, int processSize, Strategy strategy) {
        int selectedIdx = -1;

        switch (strategy) {
            case FIRST_FIT:
                for (int i = 0; i < blocks.size(); i++) {
                    if (!used[i] && blocks.get(i).getSize() >= processSize) {
                        return i;
                    }
                }
                break;

            case BEST_FIT:
                int minWaste = Integer.MAX_VALUE;
                for (int i = 0; i < blocks.size(); i++) {
                    if (!used[i] && blocks.get(i).getSize() >= processSize) {
                        int waste = blocks.get(i).getSize() - processSize;
                        if (waste < minWaste) {
                            minWaste = waste;
                            selectedIdx = i;
                        }
                    }
                }
                return selectedIdx;

            case WORST_FIT:
                int maxWaste = -1;
                for (int i = 0; i < blocks.size(); i++) {
                    if (!used[i] && blocks.get(i).getSize() >= processSize) {
                        int waste = blocks.get(i).getSize() - processSize;
                        if (waste > maxWaste) {
                            maxWaste = waste;
                            selectedIdx = i;
                        }
                    }
                }
                return selectedIdx;
        }

        return selectedIdx;
    }

    private String strategyName(Strategy s) {
        return switch (s) {
            case FIRST_FIT -> "First-Fit";
            case BEST_FIT -> "Best-Fit";
            case WORST_FIT -> "Worst-Fit";
        };
    }

    /**
     * Validate inputs.
     */
    public List<String> validate(List<MemoryBlock> blocks, int[] processSizes) {
        List<String> errors = new ArrayList<>();
        if (blocks == null || blocks.isEmpty()) {
            errors.add("At least one memory block is required.");
        } else {
            for (MemoryBlock b : blocks) {
                if (b.getSize() <= 0) errors.add("Block " + b.getBlockId() + " must have positive size.");
            }
        }
        if (processSizes == null || processSizes.length == 0) {
            errors.add("At least one process is required.");
        } else {
            for (int i = 0; i < processSizes.length; i++) {
                if (processSizes[i] <= 0) errors.add("Process " + (i + 1) + " must have positive size.");
            }
        }
        return errors;
    }
}
