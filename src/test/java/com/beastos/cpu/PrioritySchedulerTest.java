package com.beastos.cpu;

import com.beastos.cpu.engine.PriorityScheduler;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Priority (non-preemptive) scheduling algorithm.
 * Convention: Lower priority number = higher priority.
 */
class PrioritySchedulerTest {

    private PriorityScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new PriorityScheduler();
    }

    @Test
    void testAlgorithmName() {
        assertEquals("Priority", scheduler.getAlgorithmName());
    }

    @Test
    void testNormalCase() {
        // P1(AT=0,BT=10,P=3), P2(AT=0,BT=1,P=1), P3(AT=0,BT=2,P=4), P4(AT=0,BT=1,P=5), P5(AT=0,BT=5,P=2)
        List<Process> processes = List.of(
                new Process("P1", 0, 10, 3),
                new Process("P2", 0, 1, 1),
                new Process("P3", 0, 2, 4),
                new Process("P4", 0, 1, 5),
                new Process("P5", 0, 5, 2)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // Priority order (lower=higher): P2(1)→P5(2)→P1(3)→P3(4)→P4(5)
        assertResult(output.getResults(), "P2", 1, 1, 0);
        assertResult(output.getResults(), "P5", 6, 6, 1);
        assertResult(output.getResults(), "P1", 16, 16, 6);
        assertResult(output.getResults(), "P3", 18, 18, 16);
        assertResult(output.getResults(), "P4", 19, 19, 18);
    }

    @Test
    void testEqualPriorityTies() {
        // Same priority — tie-break by arrival then PID
        List<Process> processes = List.of(
                new Process("P1", 0, 3, 2),
                new Process("P2", 0, 5, 2),
                new Process("P3", 0, 4, 2)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // All same priority, same arrival → PID order: P1→P2→P3
        assertResult(output.getResults(), "P1", 3, 3, 0);
        assertResult(output.getResults(), "P2", 8, 8, 3);
        assertResult(output.getResults(), "P3", 12, 12, 8);
    }

    @Test
    void testStaggeredArrivals() {
        List<Process> processes = List.of(
                new Process("P1", 0, 4, 3),
                new Process("P2", 2, 3, 1),
                new Process("P3", 3, 2, 2)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // P1 runs first (only available at t=0), finishes at 4
        // At t=4: P2(P=1), P3(P=2) → P2 has higher priority
        // P2 finishes at 7, then P3 finishes at 9
        assertResult(output.getResults(), "P1", 4, 4, 0);
        assertResult(output.getResults(), "P2", 7, 5, 2);
        assertResult(output.getResults(), "P3", 9, 6, 4);
    }

    @Test
    void testLargeInput() {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            processes.add(new Process("P" + i, 0, 1, i + 1));
        }

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(500, output.getResults().size());
        // P0 has priority 1 (highest) → completes first at t=1
        assertResult(output.getResults(), "P0", 1, 1, 0);
    }

    private void assertResult(List<SchedulingResult> results, String pid,
                               double expectedCT, double expectedTAT, double expectedWT) {
        SchedulingResult r = results.stream()
                .filter(res -> res.getPid().equals(pid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Process " + pid + " not found"));
        assertEquals(expectedCT, r.getCompletionTime(), 0.001, pid + " CT");
        assertEquals(expectedTAT, r.getTurnaroundTime(), 0.001, pid + " TAT");
        assertEquals(expectedWT, r.getWaitingTime(), 0.001, pid + " WT");
    }
}
