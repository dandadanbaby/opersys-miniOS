package com.beastos.cpu;

import com.beastos.cpu.engine.PreemptivePriorityScheduler;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Preemptive Priority CPU scheduling algorithm.
 * Convention: Lower priority number = higher priority.
 */
class PreemptivePrioritySchedulerTest {

    private PreemptivePriorityScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new PreemptivePriorityScheduler();
    }

    @Test
    void testAlgorithmName() {
        assertEquals("Priority (Preemptive)", scheduler.getAlgorithmName());
    }

    @Test
    void testClassicPreemptivePriorityScenario() {
        // P1(AT=0, BT=4, P=3), P2(AT=1, BT=3, P=1), P3(AT=2, BT=1, P=2)
        List<Process> processes = List.of(
                new Process("P1", 0, 4, 3),
                new Process("P2", 1, 3, 1),
                new Process("P3", 2, 1, 2)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(3, output.getResults().size());

        // P2 preempts P1 at t=1, finishes at 4
        // P3 runs 4-5
        // P1 resumes 5-8
        assertResult(output.getResults(), "P2", 4.0, 3.0, 0.0);
        assertResult(output.getResults(), "P3", 5.0, 3.0, 2.0);
        assertResult(output.getResults(), "P1", 8.0, 8.0, 4.0);

        assertEquals(2.0, output.getAverageWaitingTime(), 0.001);
        assertEquals(100.0, output.getCpuUtilization(), 0.01);
    }

    @Test
    void testEqualPriorityTieBreaker() {
        // Equal priority: breaks tie by arrival time, then PID
        List<Process> processes = List.of(
                new Process("P1", 0, 3, 2),
                new Process("P2", 0, 2, 2)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        assertResult(output.getResults(), "P1", 3.0, 3.0, 0.0);
        assertResult(output.getResults(), "P2", 5.0, 5.0, 3.0);
    }

    @Test
    void testDecimalBursts() {
        List<Process> processes = List.of(
                new Process("P1", 0.0, 3.0, 2),
                new Process("P2", 1.0, 1.5, 1)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // P2 arrives at 1.0 with higher priority -> preempts P1!
        // P2 runs 1.0 - 2.5
        // P1 finishes at 2.5 + 2.0 = 4.5
        assertResult(output.getResults(), "P2", 2.5, 1.5, 0.0);
        assertResult(output.getResults(), "P1", 4.5, 4.5, 1.5);
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
