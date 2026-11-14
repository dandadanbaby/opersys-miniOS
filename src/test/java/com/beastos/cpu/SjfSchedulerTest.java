package com.beastos.cpu;

import com.beastos.cpu.engine.SjfScheduler;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SJF (Shortest Job First) non-preemptive scheduling algorithm.
 */
class SjfSchedulerTest {

    private SjfScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SjfScheduler();
    }

    @Test
    void testAlgorithmName() {
        assertEquals("SJF", scheduler.getAlgorithmName());
    }

    @Test
    void testNormalCase() {
        // P1(AT=0,BT=6), P2(AT=1,BT=8), P3(AT=2,BT=7), P4(AT=3,BT=3)
        List<Process> processes = List.of(
                new Process("P1", 0, 6),
                new Process("P2", 1, 8),
                new Process("P3", 2, 7),
                new Process("P4", 3, 3)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // P1 runs first (only one at t=0), finishes at 6
        // At t=6: P2(BT=8), P3(BT=7), P4(BT=3) → shortest is P4
        // P4 finishes at 9
        // At t=9: P2(BT=8), P3(BT=7) → shortest is P3
        // P3 finishes at 16
        // P2 finishes at 24
        assertResult(output.getResults(), "P1", 6, 6, 0);
        assertResult(output.getResults(), "P4", 9, 6, 3);
        assertResult(output.getResults(), "P3", 16, 14, 7);
        assertResult(output.getResults(), "P2", 24, 23, 15);
    }

    @Test
    void testEqualBurstTies() {
        // All same burst time — should use arrival then PID for tie-breaking
        List<Process> processes = List.of(
                new Process("P1", 0, 4),
                new Process("P2", 0, 4),
                new Process("P3", 0, 4)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(3, output.getResults().size());
        // With tie-breaking by arrival then PID: P1→P2→P3
        assertResult(output.getResults(), "P1", 4, 4, 0);
        assertResult(output.getResults(), "P2", 8, 8, 4);
        assertResult(output.getResults(), "P3", 12, 12, 8);
    }

    @Test
    void testSingleProcess() {
        List<Process> processes = List.of(new Process("P1", 0, 5));

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(1, output.getResults().size());
        assertResult(output.getResults(), "P1", 5, 5, 0);
    }

    @Test
    void testLargeInput() {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            processes.add(new Process("P" + i, 0, 500 - i)); // decreasing burst
        }

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(500, output.getResults().size());
        // Shortest burst first: P499(BT=1) should complete first
        SchedulingResult first = output.getResults().stream()
                .min((a, b) -> Integer.compare(a.getCompletionTime(), b.getCompletionTime()))
                .orElseThrow();
        assertEquals("P499", first.getPid());
        assertEquals(1, first.getCompletionTime());
    }

    private void assertResult(List<SchedulingResult> results, String pid,
                               int expectedCT, int expectedTAT, int expectedWT) {
        SchedulingResult r = results.stream()
                .filter(res -> res.getPid().equals(pid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Process " + pid + " not found"));
        assertEquals(expectedCT, r.getCompletionTime(), pid + " CT");
        assertEquals(expectedTAT, r.getTurnaroundTime(), pid + " TAT");
        assertEquals(expectedWT, r.getWaitingTime(), pid + " WT");
    }
}
