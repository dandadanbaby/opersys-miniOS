package com.beastos.integration;

import com.beastos.banker.engine.BankerEngine;
import com.beastos.banker.model.BankerState;
import com.beastos.banker.model.SafetyResult;
import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.cpu.engine.FcfsScheduler;
import com.beastos.cpu.engine.RoundRobinScheduler;
import com.beastos.cpu.engine.SjfScheduler;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.disk.engine.DiskScheduler;
import com.beastos.disk.model.DiskSchedulingOutput;
import com.beastos.fragmentation.engine.FragmentationEngine;
import com.beastos.fragmentation.model.FragmentationOutput;
import com.beastos.fragmentation.model.MemoryBlock;
import com.beastos.multiprogramming.engine.MultiprogrammingEngine;
import com.beastos.multiprogramming.model.MultiprogrammingOutput;
import com.beastos.multiprogramming.model.ProgramProcess;
import com.beastos.pagereplacement.engine.FifoReplacement;
import com.beastos.pagereplacement.engine.LruReplacement;
import com.beastos.pagereplacement.engine.OptimalReplacement;
import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.paging.engine.PagingEngine;
import com.beastos.paging.model.PagingOutput;
import com.beastos.segmentation.engine.SegmentationEngine;
import com.beastos.segmentation.model.Segment;
import com.beastos.segmentation.model.SegmentTranslationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 7: Comprehensive Integration and End-to-End System Tests for BEAST OS.
 * Validates cross-module workflows, global session state, and large stress workloads.
 */
class BeastOSIntegrationTest {

    private SimulationSession session;

    @BeforeEach
    void setUp() {
        session = SimulationSession.getInstance();
        session.reset();
    }

    @Test
    void testSimulationSessionLifecycle() {
        assertEquals(SimulationState.IDLE, session.getSimulationState());
        assertEquals(0, session.getActiveProcesses());
        assertEquals(0.0, session.getCpuUtilization());
        assertEquals(0.0, session.getMemoryInUse());
        assertEquals(0, session.getSimulationsCompleted());

        session.setSimulationState(SimulationState.RUNNING);
        assertEquals(SimulationState.RUNNING, session.getSimulationState());

        session.setActiveProcesses(6);
        assertEquals(6, session.getActiveProcesses());

        session.setCpuUtilization(72.4);
        assertEquals(72.4, session.getCpuUtilization());

        session.setMemoryInUse(0.496); // ~496 MB in GB
        assertEquals(0.496, session.getMemoryInUse());

        session.incrementSimulationsCompleted();
        assertEquals(1, session.getSimulationsCompleted());

        session.setSimulationState(SimulationState.COMPLETED);
        assertEquals(SimulationState.COMPLETED, session.getSimulationState());
    }

    @Test
    void testLiveProcessKillAndSpawnUpdatesSession() {
        session.initDefaultProcesses(List.of("P1", "P2", "P3", "P4", "P5", "P6"));
        assertEquals(6, session.getActiveProcesses());
        assertEquals(6, session.getActiveProcessNames().size());

        // Kill one process live -> count must drop from 6 to 5
        session.killProcess("P6");
        assertEquals(5, session.getActiveProcesses());
        assertEquals(5, session.getActiveProcessNames().size());
        assertFalse(session.getActiveProcessNames().contains("P6"));
        assertTrue(session.getCpuUtilization() > 0.0);
        assertTrue(session.getMemoryInUse() > 0.0);

        // Spawn a process live -> count returns to 6
        session.spawnProcess();
        assertEquals(6, session.getActiveProcesses());
        assertEquals(6, session.getActiveProcessNames().size());
    }

    @Test
    void testEndToEndAllEightSubsystemsWorkflow() {
        // 1. CPU Scheduling (Round Robin)
        RoundRobinScheduler rr = new RoundRobinScheduler(4);
        List<Process> procs = List.of(
                new Process("P1", 0, 5),
                new Process("P2", 1, 3),
                new Process("P3", 2, 8)
        );
        SchedulingOutput cpuOut = rr.schedule(procs);
        assertNotNull(cpuOut);
        assertEquals(3, cpuOut.getResults().size());
        assertTrue(cpuOut.getAverageWaitingTime() >= 0);

        // 2. Banker's Algorithm
        BankerEngine banker = new BankerEngine();
        int[][] alloc = {{0, 1, 0}, {2, 0, 0}, {3, 0, 2}, {2, 1, 1}, {0, 0, 2}};
        int[][] max = {{7, 5, 3}, {3, 2, 2}, {9, 0, 2}, {2, 2, 2}, {4, 3, 3}};
        int[] avail = {3, 3, 2};
        BankerState bState = new BankerState(alloc, max, avail);
        SafetyResult bResult = banker.checkSafety(bState);
        assertTrue(bResult.isSafe());
        assertEquals(5, bResult.getSafeSequence().size());

        // 3. Paging
        PagingEngine paging = new PagingEngine();
        int[] pageMap = {5, 6, 1, 2};
        PagingOutput pageOut = paging.translate(4096, pageMap, new int[]{0, 1024, 5000, 15000});
        assertEquals(4, pageOut.getTranslations().size());

        // 4. Page Replacement (FIFO, LRU, Optimal)
        int[] refs = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
        PageReplacementOutput fifoOut = new FifoReplacement().simulate(refs, 3);
        PageReplacementOutput lruOut = new LruReplacement().simulate(refs, 3);
        PageReplacementOutput optOut = new OptimalReplacement().simulate(refs, 3);
        assertTrue(optOut.getFaultCount() <= fifoOut.getFaultCount());
        assertTrue(optOut.getFaultCount() <= lruOut.getFaultCount());

        // 5. Segmentation
        SegmentationEngine segEngine = new SegmentationEngine();
        List<Segment> segs = List.of(
                new Segment(0, 219, 600),
                new Segment(1, 2300, 14),
                new Segment(2, 90, 100)
        );
        SegmentTranslationResult validSeg = segEngine.translate(segs, 0, 100);
        assertTrue(validSeg.isValid());
        assertEquals(319, validSeg.getPhysicalAddress());

        SegmentTranslationResult trapSeg = segEngine.translate(segs, 1, 500); // 500 > 14
        assertFalse(trapSeg.isValid());

        // 6. Fragmentation
        FragmentationEngine fragEngine = new FragmentationEngine();
        List<MemoryBlock> blocks = List.of(
                new MemoryBlock(1, 100),
                new MemoryBlock(2, 500),
                new MemoryBlock(3, 200)
        );
        FragmentationOutput fragOut = fragEngine.allocate(
                blocks,
                new String[]{"P1", "P2"},
                new int[]{80, 150},
                FragmentationEngine.Strategy.FIRST_FIT
        );
        assertEquals(2, fragOut.getAllocatedCount());
        assertEquals(0, fragOut.getWaitingCount());

        // 7. Disk Scheduling
        DiskScheduler disk = new DiskScheduler();
        int[] dQueue = {98, 183, 37, 122, 14, 124, 65, 67};
        DiskSchedulingOutput dOut = disk.schedule(dQueue, 53, 200, DiskScheduler.Algorithm.SSTF, true);
        assertEquals(236, dOut.getTotalSeekDistance());

// 8. Multiprogramming (Processor-Sharing)
        MultiprogrammingEngine multi = new MultiprogrammingEngine();
        List<ProgramProcess> mProcs = List.of(
                new ProgramProcess("j1", 10.0, 0.3),
                new ProgramProcess("j2", 10.2, 0.5),
                new ProgramProcess("j3", 10.4, 0.1)
        );
        MultiprogrammingOutput mOut = multi.simulate(mProcs);
        assertEquals(3, mOut.getTotalJobs());
        assertEquals(2, mOut.getMaxConcurrency());
        assertEquals(100.0, mOut.getCpuUtilization(), 0.01);
        assertFalse(mOut.getTraceRows().isEmpty());
    }

    @Test
    void testStressLargeWorkloadCpuScheduling() {
        FcfsScheduler fcfs = new FcfsScheduler();
        SjfScheduler sjf = new SjfScheduler();

        List<Process> largeList = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 1; i <= 1000; i++) {
            largeList.add(new Process("P" + i, rand.nextInt(500), rand.nextInt(50) + 1));
        }

        SchedulingOutput fcfsOut = fcfs.schedule(largeList);
        assertEquals(1000, fcfsOut.getResults().size());
        assertTrue(fcfsOut.getAverageTurnaroundTime() > 0);

        SchedulingOutput sjfOut = sjf.schedule(largeList);
        assertEquals(1000, sjfOut.getResults().size());
        assertTrue(sjfOut.getAverageWaitingTime() >= 0);
    }

    @Test
    void testStressLargeWorkloadDiskScheduling() {
        DiskScheduler disk = new DiskScheduler();
        int[] largeQueue = new int[1000];
        Random rand = new Random(1337);
        for (int i = 0; i < 1000; i++) {
            largeQueue[i] = rand.nextInt(5000);
        }

        DiskSchedulingOutput sstf = disk.schedule(largeQueue, 2500, 5000, DiskScheduler.Algorithm.SSTF, true);
        assertEquals(1000, sstf.getSeekSequence().size());
        assertTrue(sstf.getTotalSeekDistance() > 0);

        DiskSchedulingOutput scan = disk.schedule(largeQueue, 2500, 5000, DiskScheduler.Algorithm.SCAN, true);
        assertTrue(scan.getSeekSequence().size() >= 1000);
    }

    @Test
    void testStressLargeWorkloadPageReplacement() {
        int[] largeRefs = new int[2000];
        Random rand = new Random(999);
        for (int i = 0; i < 2000; i++) {
            largeRefs[i] = rand.nextInt(50);
        }

        PageReplacementOutput fifo = new FifoReplacement().simulate(largeRefs, 10);
        PageReplacementOutput lru = new LruReplacement().simulate(largeRefs, 10);

        assertEquals(2000, fifo.getReferenceString().length);
        assertEquals(2000, lru.getReferenceString().length);
        assertEquals(2000, fifo.getHitCount() + fifo.getFaultCount());
        assertEquals(2000, lru.getHitCount() + lru.getFaultCount());
    }
}
