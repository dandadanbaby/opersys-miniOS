package com.beastos.banker.engine;

import com.beastos.banker.model.BankerState;
import com.beastos.banker.model.SafetyResult;
import com.beastos.banker.model.SafetyResult.SafetyStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Our implementation of the Banker's Safety Algorithm!
 * We kept this pure Java with no JavaFX stuff so it's strictly the algorithm logic.
 *
 * Basically what happens here is:
 * 1. We copy Available resources into a working array (Work) and mark all processes unfinished (Finish[i] = false)
 * 2. Find any process whose remaining Need can be satisfied by current Work (Need[i] <= Work)
 *    If there are multiple, we just pick the lowest index first
 * 3. Pretend that process runs to completion and gives back its allocated resources (Work = Work + Allocation[i])
 * 4. Mark it finished and add to our safe sequence
 * 5. Keep looping until either every process finishes (system is SAFE!) or we get stuck (UNSAFE / deadlock risk)
 */
public class BankerEngine {

    // this is where we actually run the safety check on whatever BankerState is passed in
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

                // check if the system currently has enough free resources to satisfy what P[i] still needs
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

                    // we need to snapshot the state before updating work so the UI table/logs can show the step-by-step math
                    int[] workBefore = Arrays.copyOf(work, work.length);
                    int[] needI = Arrays.copyOf(need[i], need[i].length);

                    // when the process finishes, it releases everything it was holding back into Work
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

        // if any process couldn't finish, then the system isn't in a safe state
        List<String> unfinished = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!finish[i]) {
                unfinished.add("P" + i);
            }
        }

        boolean safe = unfinished.isEmpty();
        return new SafetyResult(safe, safeSequence, steps, unfinished);
    }

    /*
     * We wrote this helper to catch bad matrix inputs before running the algorithm.
     * Returns a list of error strings — if it's empty, everything is good to go!
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

        // make sure we don't have negative available resources (that wouldn't make sense physically)
        for (int j = 0; j < m; j++) {
            if (available[j] < 0) {
                errors.add("Available[" + j + "] cannot be negative.");
            }
        }

        // sanity check: a process can't hold negative resources or claim more than its declared maximum
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
