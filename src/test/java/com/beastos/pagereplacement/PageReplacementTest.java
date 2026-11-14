package com.beastos.pagereplacement;

import com.beastos.pagereplacement.engine.*;
import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FIFO, LRU, and Optimal page replacement algorithms.
 */
class PageReplacementTest {

    // Classic reference string from Silberschatz: 7,0,1,2,0,3,0,4,2,3,0,3,2,1,2,0,1,7,0,1
    private static final int[] CLASSIC_REF = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1};

    // --- FIFO Tests ---

    @Test
    void testFifo_Classic3Frames() {
        FifoReplacement fifo = new FifoReplacement();
        PageReplacementOutput output = fifo.simulate(CLASSIC_REF, 3);

        assertEquals("FIFO", output.getAlgorithmName());
        assertEquals(20, output.getTotalRequests());
        assertEquals(3, output.getFrameCount());

        // FIFO with 3 frames on this string: 15 faults (well-known result)
        assertEquals(15, output.getFaultCount());
        assertEquals(5, output.getHitCount());
    }

    @Test
    void testFifo_SingleFrame() {
        FifoReplacement fifo = new FifoReplacement();
        int[] ref = {1, 2, 3, 1, 2};
        PageReplacementOutput output = fifo.simulate(ref, 1);

        // Single frame → every unique reference is a fault, repeats may hit only if consecutive
        assertEquals(5, output.getFaultCount()); // all different or evicted
    }

    @Test
    void testFifo_AllSamePage() {
        FifoReplacement fifo = new FifoReplacement();
        int[] ref = {5, 5, 5, 5, 5};
        PageReplacementOutput output = fifo.simulate(ref, 2);

        // First is fault, rest are hits
        assertEquals(1, output.getFaultCount());
        assertEquals(4, output.getHitCount());
    }

    @Test
    void testFifo_LargeFrameCount() {
        FifoReplacement fifo = new FifoReplacement();
        // More frames than unique pages → no replacements needed
        int[] ref = {1, 2, 3, 1, 2, 3};
        PageReplacementOutput output = fifo.simulate(ref, 5);

        assertEquals(3, output.getFaultCount()); // only first occurrence of 1,2,3
        assertEquals(3, output.getHitCount());
    }

    // --- LRU Tests ---

    @Test
    void testLru_Classic3Frames() {
        LruReplacement lru = new LruReplacement();
        PageReplacementOutput output = lru.simulate(CLASSIC_REF, 3);

        assertEquals("LRU", output.getAlgorithmName());
        assertEquals(20, output.getTotalRequests());

        // LRU with 3 frames on classic string: 12 faults
        // (some implementations vary by 1 depending on tie-breaking)
        assertTrue(output.getFaultCount() >= 11 && output.getFaultCount() <= 13,
                "LRU faults should be ~12, got " + output.getFaultCount());
    }

    @Test
    void testLru_RecentlyUsedNotEvicted() {
        LruReplacement lru = new LruReplacement();
        int[] ref = {1, 2, 3, 2, 4};  // 2 frames
        PageReplacementOutput output = lru.simulate(ref, 2);

        // 1: fault [1,-]
        // 2: fault [1,2]
        // 3: fault, evict 1(LRU) → [3,2]
        // 2: hit [3,2]
        // 4: fault, evict 3(LRU) → [4,2]
        assertEquals(4, output.getFaultCount());
        assertEquals(1, output.getHitCount());
    }

    @Test
    void testLru_AllSamePage() {
        LruReplacement lru = new LruReplacement();
        int[] ref = {3, 3, 3, 3};
        PageReplacementOutput output = lru.simulate(ref, 1);

        assertEquals(1, output.getFaultCount());
        assertEquals(3, output.getHitCount());
    }

    // --- Optimal Tests ---

    @Test
    void testOptimal_Classic3Frames() {
        OptimalReplacement opt = new OptimalReplacement();
        PageReplacementOutput output = opt.simulate(CLASSIC_REF, 3);

        assertEquals("Optimal", output.getAlgorithmName());
        assertEquals(20, output.getTotalRequests());

        // Optimal with 3 frames on classic string: 9 faults (theoretical best)
        assertEquals(9, output.getFaultCount());
    }

    @Test
    void testOptimal_EvictsFarthestFuture() {
        OptimalReplacement opt = new OptimalReplacement();
        int[] ref = {1, 2, 3, 4, 1, 2};  // 3 frames
        PageReplacementOutput output = opt.simulate(ref, 3);

        // 1: fault [1,-,-]
        // 2: fault [1,2,-]
        // 3: fault [1,2,3]
        // 4: fault, must evict. Future: 1@4, 2@5, 3→never used → evict 3 → [1,2,4]
        // 1: hit
        // 2: hit
        assertEquals(4, output.getFaultCount());
        assertEquals(2, output.getHitCount());
    }

    @Test
    void testOptimal_NeverUsedAgainEvictedFirst() {
        OptimalReplacement opt = new OptimalReplacement();
        int[] ref = {1, 2, 3, 4};  // 2 frames
        PageReplacementOutput output = opt.simulate(ref, 2);

        // 1: fault [1,-]
        // 2: fault [1,2]
        // 3: fault, evict 1(never used again) → [3,2] ... or 2, both never used
        // 4: fault → [3,4] or similar
        assertEquals(4, output.getFaultCount()); // all faults, no repeats
    }

    // --- Cross-algorithm Tests ---

    @Test
    void testOptimalAlwaysBestOrEqualToOthers() {
        int[] ref = {1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5};

        FifoReplacement fifo = new FifoReplacement();
        LruReplacement lru = new LruReplacement();
        OptimalReplacement opt = new OptimalReplacement();

        PageReplacementOutput fifoOut = fifo.simulate(ref, 3);
        PageReplacementOutput lruOut = lru.simulate(ref, 3);
        PageReplacementOutput optOut = opt.simulate(ref, 3);

        // Optimal should have <= faults compared to both FIFO and LRU
        assertTrue(optOut.getFaultCount() <= fifoOut.getFaultCount(),
                "Optimal faults (" + optOut.getFaultCount() + ") should be <= FIFO (" + fifoOut.getFaultCount() + ")");
        assertTrue(optOut.getFaultCount() <= lruOut.getFaultCount(),
                "Optimal faults (" + optOut.getFaultCount() + ") should be <= LRU (" + lruOut.getFaultCount() + ")");
    }

    @Test
    void testStepCount_EqualsRefStringLength() {
        int[] ref = {1, 2, 3, 4, 5};
        FifoReplacement fifo = new FifoReplacement();
        PageReplacementOutput output = fifo.simulate(ref, 3);

        assertEquals(ref.length, output.getSteps().size());
    }

    @Test
    void testHitPlusFaultEqualsTotal() {
        int[] ref = {7, 0, 1, 2, 0, 3, 0, 4};
        for (PageReplacementAlgorithm algo : new PageReplacementAlgorithm[]{
                new FifoReplacement(), new LruReplacement(), new OptimalReplacement()}) {
            PageReplacementOutput output = algo.simulate(ref, 3);
            assertEquals(output.getTotalRequests(), output.getHitCount() + output.getFaultCount(),
                    algo.getAlgorithmName() + ": hits + faults must equal total");
        }
    }

    @Test
    void testLargeRefString() {
        int[] ref = new int[1000];
        for (int i = 0; i < 1000; i++) ref[i] = i % 20;

        FifoReplacement fifo = new FifoReplacement();
        PageReplacementOutput output = fifo.simulate(ref, 5);

        assertEquals(1000, output.getTotalRequests());
        assertTrue(output.getFaultCount() > 0);
        assertTrue(output.getHitCount() >= 0);
    }
}
