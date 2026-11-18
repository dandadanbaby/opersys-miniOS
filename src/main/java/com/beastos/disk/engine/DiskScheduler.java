package com.beastos.disk.engine;

import com.beastos.disk.model.DiskSchedulingOutput;
import com.beastos.disk.model.SeekStep;

import java.util.*;

/*
 * Hey guys, this is our disk scheduling engine for BEAST OS!
 * We implemented all 6 algorithms we covered in class: FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK.
 * Kept it pure Java with no JavaFX dependencies here so it's super easy to unit test and debug.
 */
public class DiskScheduler {

    public enum Algorithm { FCFS, SSTF, SCAN, C_SCAN, LOOK, C_LOOK }

    // this is our main entry point for disk scheduling
    // we pass in the request queue, starting head position, disk size, algorithm, and direction
    // and it runs the chosen algorithm and builds the step-by-step seek movements for our GUI table
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

        // build the step-by-step seek table so we can show each head movement in the UI
        List<SeekStep> steps = buildSteps(initialHead, sequence);

        return new DiskSchedulingOutput(algorithmName(algorithm), initialHead, cylinderLimit, sequence, steps);
    }

    // ----------------------------------------------------------------
    // FCFS — First Come, First Served
    // super straightforward: we just process requests in the exact order they arrived
    // ----------------------------------------------------------------
    private List<Integer> fcfs(int[] queue, int head) {
        List<Integer> seq = new ArrayList<>();
        for (int cyl : queue) seq.add(cyl);
        return seq;
    }

    // ----------------------------------------------------------------
    // SSTF — Shortest Seek Time First
    // we greedily find whichever track is closest to our current head position
    // our prof noted this can cause starvation for faraway requests, but it saves total seek time!
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
    // SCAN — Elevator Algorithm
    // head moves all the way in one direction servicing tracks, hits the end, and reverses
    // basically like an elevator picking people up on its way up, then reversing when it hits the top floor
    // ----------------------------------------------------------------
    private List<Integer> scan(int[] queue, int head, int cylinderLimit, boolean directionUp) {
        List<Integer> sorted = sortedCopy(queue);
        List<Integer> seq = new ArrayList<>();

        if (directionUp) {
            // service everything >= head going up, then hit the outer disk limit
            for (int cyl : sorted) {
                if (cyl >= head) seq.add(cyl);
            }
            // we have to hit the physical disk boundary before turning around
            if (seq.isEmpty() || seq.get(seq.size() - 1) != cylinderLimit) {
                seq.add(cylinderLimit);
            }
            // reverse direction: service everything < head going back down
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) < head) seq.add(sorted.get(i));
            }
        } else {
            // service everything <= head going down, then hit track 0
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) <= head) seq.add(sorted.get(i));
            }
            // hit cylinder 0 at the inner edge before turning back
            if (seq.isEmpty() || seq.get(seq.size() - 1) != 0) {
                seq.add(0);
            }
            // reverse direction: service everything > head going back up
            for (int cyl : sorted) {
                if (cyl > head) seq.add(cyl);
            }
        }
        return seq;
    }

    // ----------------------------------------------------------------
    // C-SCAN — Circular SCAN
    // like SCAN, but treats cylinders as a circular list — when it hits the end,
    // it jumps straight to the opposite end without servicing requests on the way back
    // ----------------------------------------------------------------
    private List<Integer> cscan(int[] queue, int head, int cylinderLimit, boolean directionUp) {
        List<Integer> sorted = sortedCopy(queue);
        List<Integer> seq = new ArrayList<>();

        if (directionUp) {
            // go up servicing requests, hit the end, jump to 0, and continue up
            for (int cyl : sorted) {
                if (cyl >= head) seq.add(cyl);
            }
            if (seq.isEmpty() || seq.get(seq.size() - 1) != cylinderLimit) {
                seq.add(cylinderLimit);
            }
            seq.add(0); // jump straight to beginning without servicing anything
            for (int cyl : sorted) {
                if (cyl < head) seq.add(cyl);
            }
        } else {
            // go down servicing requests, hit 0, jump to max cylinder, and continue down
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) <= head) seq.add(sorted.get(i));
            }
            if (seq.isEmpty() || seq.get(seq.size() - 1) != 0) {
                seq.add(0);
            }
            seq.add(cylinderLimit); // jump straight to the end
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (sorted.get(i) > head) seq.add(sorted.get(i));
            }
        }
        return seq;
    }

    // ----------------------------------------------------------------
    // LOOK
    // just like SCAN, but smarter! It only goes as far as the highest/lowest requested cylinder,
    // so we don't waste time seeking all the way to the disk edge if nobody asked for it
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
    // C-LOOK — Circular LOOK
    // combines circular scanning with LOOK: goes only to the last request in that direction,
    // then jumps directly to the lowest requested cylinder instead of going all the way to 0
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
    // our little helper functions
    // ----------------------------------------------------------------
    private List<Integer> sortedCopy(int[] queue) {
        List<Integer> list = new ArrayList<>();
        for (int c : queue) list.add(c);
        Collections.sort(list);
        return list;
    }

    // this converts the raw sequence of numbers into nice step objects for our GUI table
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

    // we added this input validation so bad user inputs don't crash the simulation
    // checks for negative numbers, head out of bounds, empty queue, etc.
    public List<String> validate(int[] requestQueue, int initialHead, int cylinderLimit) {
        List<String> errors = new ArrayList<>();
        if (cylinderLimit <= 0) {
            errors.add("Cylinder limit must be positive.");
        }
        if (initialHead < 0) {
            errors.add("Initial head position cannot be negative.");
        }
        if (cylinderLimit > 0 && initialHead > cylinderLimit) {
            errors.add("Initial head (" + initialHead + ") must be within cylinder limit (" + cylinderLimit + ").");
        }
        if (requestQueue == null || requestQueue.length == 0) {
            errors.add("Request queue must have at least one entry.");
        } else {
            for (int i = 0; i < requestQueue.length; i++) {
                if (requestQueue[i] < 0) {
                    errors.add("Request " + (i + 1) + ": cylinder cannot be negative.");
                } else if (cylinderLimit > 0 && requestQueue[i] > cylinderLimit) {
                    errors.add("Request " + (i + 1) + ": cylinder " + requestQueue[i]
                            + " exceeds limit (" + cylinderLimit + ").");
                }
            }
        }
        return errors;
    }
}
