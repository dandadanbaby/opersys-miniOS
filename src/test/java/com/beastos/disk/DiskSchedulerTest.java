package com.beastos.disk;

import com.beastos.disk.engine.DiskScheduler;
import com.beastos.disk.engine.DiskScheduler.Algorithm;
import com.beastos.disk.model.DiskSchedulingOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all 6 disk scheduling algorithms.
 * Reference example from the design: queue=[98,183,37,122,14,124,65,67], head=53, limit=200.
 */
class DiskSchedulerTest {

    private DiskScheduler scheduler;

    // Design reference dataset
    private static final int[] QUEUE = {98, 183, 37, 122, 14, 124, 65, 67};
    private static final int HEAD = 53;
    private static final int LIMIT = 200;

    @BeforeEach
    void setUp() {
        scheduler = new DiskScheduler();
    }

    // ----------------------------------------------------------------
    //  FCFS Tests
    // ----------------------------------------------------------------
    @Test
    void testFcfsOrder() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.FCFS, true);
        assertEquals(List.of(98, 183, 37, 122, 14, 124, 65, 67), out.getSeekSequence());
        assertEquals("FCFS", out.getAlgorithmName());
    }

    @Test
    void testFcfsTotalSeek() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.FCFS, true);
        // |53-98|=45, |98-183|=85, |183-37|=146, |37-122|=85, |122-14|=108, |14-124|=110, |124-65|=59, |65-67|=2
        // Total = 45+85+146+85+108+110+59+2 = 640
        assertEquals(640, out.getTotalSeekDistance());
    }

    @Test
    void testFcfsSingleRequest() {
        DiskSchedulingOutput out = scheduler.schedule(new int[]{100}, 50, 200, Algorithm.FCFS, true);
        assertEquals(List.of(100), out.getSeekSequence());
        assertEquals(50, out.getTotalSeekDistance());
    }

    // ----------------------------------------------------------------
    //  SSTF Tests
    // ----------------------------------------------------------------
    @Test
    void testSstfOrder() {
        // From design reference: SSTF with head=53, queue=[98,183,37,122,14,124,65,67]
        // Expected: 53 → 65 → 67 → 37 → 14 → 98 → 122 → 124 → 183
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.SSTF, true);
        assertEquals(List.of(65, 67, 37, 14, 98, 122, 124, 183), out.getSeekSequence());
    }

    @Test
    void testSstfTotalSeek() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.SSTF, true);
        // 53→65=12, 65→67=2, 67→37=30, 37→14=23, 14→98=84, 98→122=24, 122→124=2, 124→183=59
        // Total = 12+2+30+23+84+24+2+59 = 236
        assertEquals(236, out.getTotalSeekDistance());
    }

    @Test
    void testSstfAvgSeek() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.SSTF, true);
        assertEquals(29.5, out.getAverageSeekDistance(), 0.01);
    }

    // ----------------------------------------------------------------
    //  SCAN Tests
    // ----------------------------------------------------------------
    @Test
    void testScanUpOrder() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.SCAN, true);
        // Going up: 65, 67, 98, 122, 124, 183, then hit 199 (limit-1), then go down: 37, 14
        List<Integer> seq = out.getSeekSequence();
        assertTrue(seq.contains(199), "SCAN should hit the disk boundary");
        // First elements should be ascending from head
        assertEquals(65, seq.get(0));
        assertEquals(67, seq.get(1));
    }

    @Test
    void testScanDownOrder() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.SCAN, false);
        // Going down: 37, 14, then hit 0, then go up: 65, 67, 98, 122, 124, 183
        List<Integer> seq = out.getSeekSequence();
        assertTrue(seq.contains(0), "SCAN down should hit cylinder 0");
        assertEquals(37, seq.get(0));
        assertEquals(14, seq.get(1));
    }

    // ----------------------------------------------------------------
    //  C-SCAN Tests
    // ----------------------------------------------------------------
    @Test
    void testCscanUpOrder() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.C_SCAN, true);
        List<Integer> seq = out.getSeekSequence();
        // Up: 65, 67, 98, 122, 124, 183, 199, jump to 0, then 14, 37
        assertTrue(seq.contains(199));
        assertTrue(seq.contains(0));
        // 14 and 37 should come after 0
        int zeroIdx = seq.indexOf(0);
        assertTrue(seq.indexOf(14) > zeroIdx);
        assertTrue(seq.indexOf(37) > zeroIdx);
    }

    @Test
    void testCscanServicesAll() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.C_SCAN, true);
        for (int cyl : QUEUE) {
            assertTrue(out.getSeekSequence().contains(cyl),
                    "C-SCAN must service all requests, missing: " + cyl);
        }
    }

    // ----------------------------------------------------------------
    //  LOOK Tests
    // ----------------------------------------------------------------
    @Test
    void testLookUpDoesNotHitBoundary() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.LOOK, true);
        List<Integer> seq = out.getSeekSequence();
        // LOOK should NOT go to 199 (disk boundary), only to highest request (183)
        assertFalse(seq.contains(199), "LOOK should not hit disk boundary");
        assertTrue(seq.contains(183));
    }

    @Test
    void testLookUpOrder() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.LOOK, true);
        List<Integer> seq = out.getSeekSequence();
        // Up: 65, 67, 98, 122, 124, 183, then reverse: 37, 14
        assertEquals(65, seq.get(0));
        assertEquals(183, seq.get(5));
        assertEquals(37, seq.get(6));
        assertEquals(14, seq.get(7));
    }

    // ----------------------------------------------------------------
    //  C-LOOK Tests
    // ----------------------------------------------------------------
    @Test
    void testClookUpOrder() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.C_LOOK, true);
        List<Integer> seq = out.getSeekSequence();
        // Up: 65, 67, 98, 122, 124, 183, then jump to lowest: 14, 37
        assertFalse(seq.contains(199), "C-LOOK should not hit disk boundary");
        assertFalse(seq.contains(0), "C-LOOK should not hit 0");
        // After 183 should come 14
        int idx183 = seq.indexOf(183);
        assertEquals(14, seq.get(idx183 + 1));
    }

    @Test
    void testClookServicesAll() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.C_LOOK, true);
        for (int cyl : QUEUE) {
            assertTrue(out.getSeekSequence().contains(cyl),
                    "C-LOOK must service all requests, missing: " + cyl);
        }
    }

    // ----------------------------------------------------------------
    //  Validation Tests
    // ----------------------------------------------------------------
    @Test
    void testValidateNegativeHead() {
        List<String> errors = scheduler.validate(QUEUE, -1, 200);
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidateHeadExceedsLimit() {
        List<String> errors = scheduler.validate(QUEUE, 200, 200);
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidateRequestExceedsLimit() {
        List<String> errors = scheduler.validate(new int[]{50, 250}, 0, 200);
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidateEmptyQueue() {
        List<String> errors = scheduler.validate(new int[]{}, 50, 200);
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidateValidInput() {
        List<String> errors = scheduler.validate(QUEUE, HEAD, LIMIT);
        assertTrue(errors.isEmpty());
    }

    // ----------------------------------------------------------------
    //  Step-by-step table tests
    // ----------------------------------------------------------------
    @Test
    void testStepCountMatchesSequence() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.SSTF, true);
        assertEquals(out.getSeekSequence().size(), out.getSteps().size());
    }

    @Test
    void testStepDistanceSumEqualsTotal() {
        DiskSchedulingOutput out = scheduler.schedule(QUEUE, HEAD, LIMIT, Algorithm.FCFS, true);
        int sumSteps = out.getSteps().stream().mapToInt(s -> s.getSeekDistance()).sum();
        assertEquals(out.getTotalSeekDistance(), sumSteps);
    }
}
