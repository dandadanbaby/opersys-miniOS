package com.beastos.cpu;

import com.beastos.cpu.engine.FcfsScheduler;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FCFS (First Come First Serve) scheduling algorithm.
 */
class FcfsSchedulerTest {

    private FcfsScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new FcfsScheduler();
    }

    @Test
    void testAlgorithmName() {
        assertEquals("FCFS", scheduler.getAlgorithmName());
    }

    @Test
    void testNormalCase_FourProcesses() {
        // P1(AT=0,BT=5), P2(AT=1,BT=3), P3(AT=2,BT=8), P4(AT=3,BT=6)
        List<Process> processes = List.of(
                new Process("P1", 0, 5),
                new Process("P2", 1, 3),
                new Process("P3", 2, 8),
                new Process("P4", 3, 6)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        List<SchedulingResult> results = output.getResults();
        assertEquals(4, results.size());

        // FCFS order: P1→P2→P3→P4
        // P1: CT=5, TAT=5, WT=0
        assertResult(results, "P1", 5, 5, 0);
        // P2: CT=8, TAT=7, WT=4
        assertResult(results, "P2", 8, 7, 4);
        // P3: CT=16, TAT=14, WT=6
        assertResult(results, "P3", 16, 14, 6);
        // P4: CT=22, TAT=19, WT=13
        assertResult(results, "P4", 22, 19, 13);
    }

    @Test
    void testSingleProcess() {
        List<Process> processes = List.of(new Process("P1", 0, 10));

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(1, output.getResults().size());
        assertResult(output.getResults(), "P1", 10, 10, 0);
        assertEquals(100.0, output.getCpuUtilization(), 0.01);
    }

    @Test
    void testAllArriveAtZero() {
        List<Process> processes = List.of(
                new Process("P1", 0, 3),
                new Process("P2", 0, 5),
                new Process("P3", 0, 2)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // Tie-break by PID: P1→P2→P3
        // P1: CT=3, TAT=3-0=3, WT=3-3=0
        // P2: CT=8, TAT=8-0=8, WT=8-5=3
        // P3: CT=10, TAT=10-0=10, WT=10-2=8
        assertResult(output.getResults(), "P1", 3, 3, 0);
        assertResult(output.getResults(), "P2", 8, 8, 3);
        assertResult(output.getResults(), "P3", 10, 10, 8);
    }

    @Test
    void testIdleGap() {
        // P1 arrives at 0, P2 arrives at 10 → idle gap between 3-10
        List<Process> processes = List.of(
                new Process("P1", 0, 3),
                new Process("P2", 10, 5)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        assertResult(output.getResults(), "P1", 3, 3, 0);
        assertResult(output.getResults(), "P2", 15, 5, 0);

        // Should have an IDLE entry in Gantt
        assertTrue(output.getGanttChart().stream().anyMatch(g -> g.isIdle()));
        // CPU utilization < 100% due to idle
        assertTrue(output.getCpuUtilization() < 100.0);
    }

    @Test
    void testDecimalTimes() {
        // P1(AT=0.5, BT=2.5), P2(AT=1.0, BT=1.5)
        List<Process> processes = List.of(
                new Process("P1", 0.5, 2.5),
                new Process("P2", 1.0, 1.5)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // P1 arrives at 0.5, finishes at 0.5 + 2.5 = 3.0. CT=3.0, TAT=2.5, WT=0.0
        // P2 waits until 3.0, finishes at 3.0 + 1.5 = 4.5. CT=4.5, TAT=3.5, WT=2.0
        assertResult(output.getResults(), "P1", 3.0, 2.5, 0.0);
        assertResult(output.getResults(), "P2", 4.5, 3.5, 2.0);
    }

    @Test
    void testLargeInput_1000Processes() {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            processes.add(new Process("P" + i, i, 1));
        }

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(1000, output.getResults().size());
        // All waiting times should be 0 since each arrives just as previous finishes
        for (SchedulingResult r : output.getResults()) {
            assertEquals(0, r.getWaitingTime());
        }
    }

    private void assertResult(List<SchedulingResult> results, String pid,
                               double expectedCT, double expectedTAT, double expectedWT) {
        SchedulingResult r = results.stream()
                .filter(res -> res.getPid().equals(pid))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Process " + pid + " not found"));
        assertEquals(expectedCT, r.getCompletionTime(), 0.001, pid + " CT");
        assertEquals(expectedTAT, r.getTurnaroundTime(), 0.001, pid + " TAT");
        assertEquals(expectedWT, r.getWaitingTime(), 0.001, pid + " WT");
    }

    // Fix the inner class name — Java doesn't have AssertionError, it's AssertionError
    // Actually the standard class is AssertionError — let me use RuntimeException
    private static class AssertionError extends RuntimeException {
        AssertionError(String msg) { super(msg); }
    }
}
