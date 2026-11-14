package com.beastos.banker.engine;

import com.beastos.banker.model.BankerState;
import com.beastos.banker.model.SafetyResult;
import com.beastos.banker.model.SafetyResult.SafetyStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Banker's Safety Algorithm implementation.
 * Pure Java — no JavaFX dependency.
 *
 * Algorithm:
 * 1. Work = Available, Finish[i] = false for all i
 * 2. Find process i where Finish[i] == false AND Need[i] <= Work
 *    (tie-breaking: lowest index first)
 * 3. Work = Work + Allocation[i], Finish[i] = true, add to safe sequence
 * 4. Repeat step 2 until all finished (SAFE) or no process can be found (UNSAFE)
 */
public class BankerEngine {

    /**
     * Run the safety algorithm on the given state.
     */
    public SafetyResult checkSafety(BankerState state) {
        int n = state.getProcesses();
        int m = state.getResources();

        int[] work = state.getAvailable();
        boolean[] finish = new boolean[n];
        int[][] need = state.getNeed();
        int[][] allocation = state.getAllocation();

        List<String> safeSequence = new ArrayList<>();
        List<SafetyStep> steps = new ArrayList<>();
        int stepNum = 0;

        boolean found = true;
        while (found) {
            found = false;
            for (int i = 0; i < n; i++) {
                if (finish[i]) continue;

                // Check if Need[i] <= Work
                boolean canExecute = true;
                for (int j = 0; j < m; j++) {
                    if (need[i][j] > work[j]) {
                        canExecute = false;
                        break;
                    }
                }

                if (canExecute) {
                    stepNum++;
                    String pid = "P" + i;

                    // Record step BEFORE updating work
                    int[] workBefore = Arrays.copyOf(work, work.length);
                    int[] needI = Arrays.copyOf(need[i], need[i].length);

                    // Execute: Work = Work + Allocation[i]
                    for (int j = 0; j < m; j++) {
                        work[j] += allocation[i][j];
                    }
                    finish[i] = true;

                    int[] workAfter = Arrays.copyOf(work, work.length);
                    steps.add(new SafetyStep(stepNum, pid, needI, workBefore, true, workAfter));
                    safeSequence.add(pid);
                    found = true;
                }
            }
        }

        // Check if all processes finished
        List<String> unfinished = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!finish[i]) {
                unfinished.add("P" + i);
            }
        }

        boolean safe = unfinished.isEmpty();
        return new SafetyResult(safe, safeSequence, steps, unfinished);
    }

    /**
     * Validate that the banker state is consistent.
     * Returns a list of error messages (empty if valid).
     */
    public List<String> validate(int[][] allocation, int[][] maximum, int[] available) {
        List<String> errors = new ArrayList<>();

        if (allocation.length == 0) {
            errors.add("At least one process is required.");
            return errors;
        }
        if (allocation[0].length == 0) {
            errors.add("At least one resource type is required.");
            return errors;
        }

        int n = allocation.length;
        int m = allocation[0].length;

        if (maximum.length != n) {
            errors.add("Allocation and Maximum must have the same number of processes.");
            return errors;
        }

        for (int i = 0; i < n; i++) {
            if (allocation[i].length != m || maximum[i].length != m) {
                errors.add("All rows must have " + m + " resource columns.");
                return errors;
            }
        }

        if (available.length != m) {
            errors.add("Available vector must have " + m + " entries.");
            return errors;
        }

        // Check non-negative values
        for (int j = 0; j < m; j++) {
            if (available[j] < 0) {
                errors.add("Available[" + j + "] cannot be negative.");
            }
        }

        // Check allocation <= maximum
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (allocation[i][j] < 0) {
                    errors.add("Allocation[P" + i + "][R" + j + "] cannot be negative.");
                }
                if (maximum[i][j] < 0) {
                    errors.add("Maximum[P" + i + "][R" + j + "] cannot be negative.");
                }
                if (allocation[i][j] > maximum[i][j]) {
                    errors.add("Allocation[P" + i + "][R" + j + "] = " + allocation[i][j] +
                            " exceeds Maximum[P" + i + "][R" + j + "] = " + maximum[i][j]);
                }
            }
        }

        return errors;
    }
}
