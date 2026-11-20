package com.beastos.cpu;

import com.beastos.cpu.engine.SrtfScheduler;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SRTF (Shortest Remaining Time First) preemptive scheduling algorithm.
 */
class SrtfSchedulerTest {

    private SrtfScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SrtfScheduler();
    }

    @Test
    void testAlgorithmName() {
        assertEquals("SRTF", scheduler.getAlgorithmName());
    }

    @Test
    void testClassicTextbookPreemption() {
        // P1(AT=0, BT=8), P2(AT=1, BT=4), P3(AT=2, BT=9), P4(AT=3, BT=5)
        List<Process> processes = List.of(
                new Process("P1", 0, 8),
                new Process("P2", 1, 4),
                new Process("P3", 2, 9),
                new Process("P4", 3, 5)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        assertEquals(4, output.getResults().size());

        // P2 preempts P1 at t=1, finishes at 5
        // P4 runs 5-10
        // P1 resumes 10-17
        // P3 runs 17-26
        assertResult(output.getResults(), "P2", 5.0, 4.0, 0.0);
        assertResult(output.getResults(), "P4", 10.0, 7.0, 2.0);
        assertResult(output.getResults(), "P1", 17.0, 17.0, 9.0);
        assertResult(output.getResults(), "P3", 26.0, 24.0, 15.0);

        assertEquals(6.5, output.getAverageWaitingTime(), 0.001);
        assertEquals(13.0, output.getAverageTurnaroundTime(), 0.001);
        assertEquals(100.0, output.getCpuUtilization(), 0.01);
    }

    @Test
    void testDecimalBurstAndArrival() {
        // P1(AT=0.0, BT=2.5), P2(AT=0.5, BT=1.0)
        List<Process> processes = List.of(
                new Process("P1", 0.0, 2.5),
                new Process("P2", 0.5, 1.0)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        // At t=0.5, P1 has 2.0 left. P2 has 1.0 -> P2 preempts P1!
        // P2 finishes at 0.5 + 1.0 = 1.5
        // P1 finishes at 1.5 + 2.0 = 3.5
        assertResult(output.getResults(), "P2", 1.5, 1.0, 0.0);
        assertResult(output.getResults(), "P1", 3.5, 3.5, 1.0);
    }

    @Test
    void testIdleGap() {
        List<Process> processes = List.of(
                new Process("P1", 0.0, 2.0),
                new Process("P2", 5.0, 3.0)
        );

        SchedulingOutput output = scheduler.schedule(processes);
        assertResult(output.getResults(), "P1", 2.0, 2.0, 0.0);
        assertResult(output.getResults(), "P2", 8.0, 3.0, 0.0);
        assertTrue(output.getGanttChart().stream().anyMatch(g -> g.isIdle()));
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
