package com.beastos.banker.model;

import java.util.Arrays;
import java.util.List;

/* this class holds the final verdict from our banker's safety algorithm!
   we store whether the system is in a safe state, the safe execution sequence if one was found,
   and each step along the way so we can display the whole walkthrough in the UI */
public class SafetyResult {
    private final boolean safe;
    private final List<String> safeSequence;    // the winning order of processes, e.g. ["P1", "P3", "P4", "P0", "P2"]
    private final List<SafetyStep> steps;        // we keep a log of each step so we can show our work in the UI table
    private final List<String> unfinishedProcesses; // if unsafe, we track which processes got stuck and couldn't finish

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

    /* we made this inner class to capture a snapshot of every single check during the algorithm.
       in class we learned you have to check Need <= Work, so we record that comparison for each process */
    public static class SafetyStep {
        private final int stepNumber;
        private final String processId;
        private final int[] need;
        private final int[] work;
        private final boolean needLessOrEqualWork;
        private final int[] workAfter; // what available resources become after this process finishes and returns its allocation
        private final String description;

        public SafetyStep(int stepNumber, String processId, int[] need, int[] work,
                          boolean needLessOrEqualWork, int[] workAfter) {
            this.stepNumber = stepNumber;
            this.processId = processId;
            this.need = Arrays.copyOf(need, need.length);
            this.work = Arrays.copyOf(work, work.length);
            this.needLessOrEqualWork = needLessOrEqualWork;
            this.workAfter = workAfter != null ? Arrays.copyOf(workAfter, workAfter.length) : null;

            // we build a nice readable string here so we can dump it straight to the UI log
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
