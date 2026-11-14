package com.beastos.disk.engine;

import com.beastos.disk.model.DiskSchedulingOutput;
import com.beastos.disk.model.SeekStep;

import java.util.*;

/**
 * Disk scheduling engine implementing 6 algorithms:
 * FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK.
 *
 * Pure Java — no JavaFX dependency.
 */
public class DiskScheduler {

    public enum Algorithm { FCFS, SSTF, SCAN, C_SCAN, LOOK, C_LOOK }

    /**
     * Schedule disk requests using the specified algorithm.
     *
     * @param requestQueue   cylinder numbers in the request queue
     * @param initialHead    starting position of the disk head
     * @param cylinderLimit  maximum cylinder number (0 to cylinderLimit-1)
     * @param algorithm      scheduling algorithm
     * @param directionUp    for SCAN/C-SCAN/LOOK/C-LOOK: true = head moves toward higher cylinders first
     * @return complete scheduling output with seek sequence, steps, and distances
     */
    public DiskSchedulingOutput schedule(int[] requestQueue, int initialHead,
                                          int cylinderLimit, Algorithm algorithm, boolean directionUp) {
        List<Integer> sequence;
        switch (algorithm) {
            case FCFS:   sequence = fcfs(requestQueue, initialHead); break;
            case SSTF:   sequence = sstf(requestQueue, initialHead); break;
            case SCAN:   sequence = scan(requestQueue, initialHead, cylinderLimit, directionUp); break;
            case C_SCAN: sequence = cscan(requestQueue, initialHead, cylinderLimit, directionUp); break;
            case LOOK:   sequence = look(requestQueue, initialHead, directionUp); break;
            case C_LOOK: sequence = clook(requestQueue, initialHead, directionUp); break;
            default:     sequence = fcfs(requestQueue, initialHead); break;
        }

        // Build step-by-step seek table
        List<SeekStep> steps = buildSteps(initialHead, sequence);

        return new DiskSchedulingOutput(algorithmName(algorithm), initialHead, cylinderLimit, sequence, steps);
    }

    // ----------------------------------------------------------------
    //  FCFS — First Come, First Served
    // ----------------------------------------------------------------
    private List<Integer> fcfs(int[] queue, int head) {
        List<Integer> seq = new ArrayList<>();
        for (int cyl : queue) seq.add(cyl);
        return seq;
    }

    // ----------------------------------------------------------------
    //  SSTF — Shortest Seek Time First
    // ----------------------------------------------------------------
    private List<Integer> sstf(int[] queue, int head) {
        List<Integer> remaining = new ArrayList<>();
        for (int cyl : queue) remaining.add(cyl);
        List<Integer> seq = new ArrayList<>();
        int current = head;

        while (!remaining.isEmpty()) {
            int closest = -1;
            int minDist = Integer.MAX_VALUE;
            for (int i = 0; i < remaining.size(); i++) {
                int dist = Math.abs(remaining.get(i) - current);
                if (dist < minDist) {
                    minDist = dist;
                    closest = i;
                }
            }
            int next = remaining.remove(closest);
            seq.add(next);
            current = next;
        }
        return seq;
    }

    // ----------------------------------------------------------------
    //  SCAN — Elevator Algorithm
    // ----------------------------------------------------------------
    private List<Integer> scan(int[] queue, int head, int cylinderLimit, boolean directionUp) {
        List<Integer> sorted = sortedCopy(queue);
        List<Integer> seq = new ArrayList<>();

        if (directionUp) {
            // Service requests >= head going up, then hit end, then service remaining going down
            for (int cyl : sorted) {
                if (cyl >= head) seq.add(cyl);
            }
            // Hit the end of the disk
            if (seq.isEmpty() || seq.get(seq.size() - 1) != cylinderLimit - 1) {
                seq.add(cylinderLimit - 1);
            }
            // Reverse: service requests < head going down
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) < head) seq.add(sorted.get(i));
            }
        } else {
            // Service requests <= head going down, then hit 0, then service remaining going up
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) <= head) seq.add(sorted.get(i));
            }
            // Hit cylinder 0
            if (seq.isEmpty() || seq.get(seq.size() - 1) != 0) {
                seq.add(0);
            }
            // Forward: service requests > head going up
            for (int cyl : sorted) {
                if (cyl > head) seq.add(cyl);
            }
        }
        return seq;
    }

    // ----------------------------------------------------------------
    //  C-SCAN — Circular SCAN
    // ----------------------------------------------------------------
    private List<Integer> cscan(int[] queue, int head, int cylinderLimit, boolean directionUp) {
        List<Integer> sorted = sortedCopy(queue);
        List<Integer> seq = new ArrayList<>();

        if (directionUp) {
            // Go up servicing requests, hit end, jump to 0, continue up
            for (int cyl : sorted) {
                if (cyl >= head) seq.add(cyl);
            }
            if (seq.isEmpty() || seq.get(seq.size() - 1) != cylinderLimit - 1) {
                seq.add(cylinderLimit - 1);
            }
            seq.add(0); // jump to beginning
            for (int cyl : sorted) {
                if (cyl < head) seq.add(cyl);
            }
        } else {
            // Go down servicing requests, hit 0, jump to end, continue down
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) <= head) seq.add(sorted.get(i));
            }
            if (seq.isEmpty() || seq.get(seq.size() - 1) != 0) {
                seq.add(0);
            }
            seq.add(cylinderLimit - 1); // jump to end
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) > head) seq.add(sorted.get(i));
            }
        }
        return seq;
    }

    // ----------------------------------------------------------------
    //  LOOK — Like SCAN but doesn't go to disk boundaries
    // ----------------------------------------------------------------
    private List<Integer> look(int[] queue, int head, boolean directionUp) {
        List<Integer> sorted = sortedCopy(queue);
        List<Integer> seq = new ArrayList<>();

        if (directionUp) {
            for (int cyl : sorted) {
                if (cyl >= head) seq.add(cyl);
            }
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) < head) seq.add(sorted.get(i));
            }
        } else {
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) <= head) seq.add(sorted.get(i));
            }
            for (int cyl : sorted) {
                if (cyl > head) seq.add(cyl);
            }
        }
        return seq;
    }

    // ----------------------------------------------------------------
    //  C-LOOK — Circular LOOK
    // ----------------------------------------------------------------
    private List<Integer> clook(int[] queue, int head, boolean directionUp) {
        List<Integer> sorted = sortedCopy(queue);
        List<Integer> seq = new ArrayList<>();

        if (directionUp) {
            for (int cyl : sorted) {
                if (cyl >= head) seq.add(cyl);
            }
            for (int cyl : sorted) {
                if (cyl < head) seq.add(cyl);
            }
        } else {
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) <= head) seq.add(sorted.get(i));
            }
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) > head) seq.add(sorted.get(i));
            }
        }
        return seq;
    }

    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------
    private List<Integer> sortedCopy(int[] queue) {
        List<Integer> list = new ArrayList<>();
        for (int c : queue) list.add(c);
        Collections.sort(list);
        return list;
    }

    private List<SeekStep> buildSteps(int initialHead, List<Integer> sequence) {
        List<SeekStep> steps = new ArrayList<>();
        int prev = initialHead;
        for (int i = 0; i < sequence.size(); i++) {
            int target = sequence.get(i);
            String label;
            if (i == 0) {
                label = "Initial → P1";
            } else {
                label = "P" + i + " → P" + (i + 1);
            }
            steps.add(new SeekStep(i + 1, label, prev, target));
            prev = target;
        }
        return steps;
    }

    private String algorithmName(Algorithm a) {
        return switch (a) {
            case FCFS -> "FCFS";
            case SSTF -> "SSTF";
            case SCAN -> "SCAN";
            case C_SCAN -> "C-SCAN";
            case LOOK -> "LOOK";
            case C_LOOK -> "C-LOOK";
        };
    }

    /**
     * Validate disk scheduling inputs.
     */
    public List<String> validate(int[] requestQueue, int initialHead, int cylinderLimit) {
        List<String> errors = new ArrayList<>();
        if (cylinderLimit <= 0) {
            errors.add("Cylinder limit must be positive.");
        }
        if (initialHead < 0) {
            errors.add("Initial head position cannot be negative.");
        }
        if (cylinderLimit > 0 && initialHead >= cylinderLimit) {
            errors.add("Initial head (" + initialHead + ") must be less than cylinder limit (" + cylinderLimit + ").");
        }
        if (requestQueue == null || requestQueue.length == 0) {
            errors.add("Request queue must have at least one entry.");
        } else {
            for (int i = 0; i < requestQueue.length; i++) {
                if (requestQueue[i] < 0) {
                    errors.add("Request " + (i + 1) + ": cylinder cannot be negative.");
                } else if (cylinderLimit > 0 && requestQueue[i] >= cylinderLimit) {
                    errors.add("Request " + (i + 1) + ": cylinder " + requestQueue[i]
                            + " exceeds limit (" + cylinderLimit + ").");
                }
            }
        }
        return errors;
    }
}
