package com.beastos.banker.model;

import java.util.Arrays;
import java.util.List;

/**
 * Result of the Banker's Safety Algorithm.
 */
public class SafetyResult {
    private final boolean safe;
    private final List<String> safeSequence;    // e.g. ["P1", "P3", "P4", "P0", "P2"]
    private final List<SafetyStep> steps;        // detailed step-by-step log
    private final List<String> unfinishedProcesses; // processes that couldn't finish (unsafe)

    public SafetyResult(boolean safe, List<String> safeSequence,
                        List<SafetyStep> steps, List<String> unfinishedProcesses) {
        this.safe = safe;
        this.safeSequence = List.copyOf(safeSequence);
        this.steps = List.copyOf(steps);
        this.unfinishedProcesses = List.copyOf(unfinishedProcesses);
    }

    public boolean isSafe() { return safe; }
    public List<String> getSafeSequence() { return safeSequence; }
    public List<SafetyStep> getSteps() { return steps; }
    public List<String> getUnfinishedProcesses() { return unfinishedProcesses; }

    /**
     * One step of the safety check verification log.
     */
    public static class SafetyStep {
        private final int stepNumber;
        private final String processId;
        private final int[] need;
        private final int[] work;
        private final boolean needLessOrEqualWork;
        private final int[] workAfter; // work after executing this process
        private final String description;

        public SafetyStep(int stepNumber, String processId, int[] need, int[] work,
                          boolean needLessOrEqualWork, int[] workAfter) {
            this.stepNumber = stepNumber;
            this.processId = processId;
            this.need = Arrays.copyOf(need, need.length);
            this.work = Arrays.copyOf(work, work.length);
            this.needLessOrEqualWork = needLessOrEqualWork;
            this.workAfter = workAfter != null ? Arrays.copyOf(workAfter, workAfter.length) : null;

            // Build description
            StringBuilder sb = new StringBuilder();
            sb.append("Step ").append(stepNumber).append(": ").append(processId);
            sb.append(" [Need: ").append(Arrays.toString(need));
            sb.append(" <= Work: ").append(Arrays.toString(work)).append("]");
            if (needLessOrEqualWork) {
                sb.append(" → Executing ").append(processId);
                sb.append(" → Available becomes ").append(Arrays.toString(workAfter));
                sb.append("  (Finish ").append(processId).append(")");
            }
            this.description = sb.toString();
        }

        public int getStepNumber() { return stepNumber; }
        public String getProcessId() { return processId; }
        public int[] getNeed() { return Arrays.copyOf(need, need.length); }
        public int[] getWork() { return Arrays.copyOf(work, work.length); }
        public boolean isNeedLessOrEqualWork() { return needLessOrEqualWork; }
        public int[] getWorkAfter() { return workAfter != null ? Arrays.copyOf(workAfter, workAfter.length) : null; }
        public String getDescription() { return description; }
    }
}
