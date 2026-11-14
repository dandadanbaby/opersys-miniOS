package com.beastos.fragmentation;

import com.beastos.fragmentation.engine.FragmentationEngine;
import com.beastos.fragmentation.engine.FragmentationEngine.Strategy;
import com.beastos.fragmentation.model.AllocationResult;
import com.beastos.fragmentation.model.FragmentationOutput;
import com.beastos.fragmentation.model.MemoryBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Fragmentation engine: First-Fit, Best-Fit, Worst-Fit.
 */
class FragmentationEngineTest {

    private FragmentationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new FragmentationEngine();
    }

    @Test
    void testFirstFit_DesignReferenceCase() {
        // From design: Blocks 100,150,370,180,50 KB; Processes P1=80,P2=120,P3=320,P4=150,P5=80,P6=30
        List<MemoryBlock> blocks = List.of(
                new MemoryBlock(1, 100),
                new MemoryBlock(2, 150),
                new MemoryBlock(3, 370),
                new MemoryBlock(4, 180),
                new MemoryBlock(5, 50)
        );
        String[] pids = {"P1", "P2", "P3", "P4", "P5", "P6"};
        int[] sizes = {80, 120, 320, 150, 80, 30};

        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.FIRST_FIT);

        assertEquals("First-Fit", output.getAlgorithmName());
        assertEquals(6, output.getTotalProcesses());

        // First-Fit: P1→Block1(100), P2→Block2(150), P3→Block3(370), P4→Block4(180)
        // P5 needs 80: Block5 has 50 → too small. No blocks left → Waiting
        // P6 needs 30: Block5 has 50 → fits
        assertAllocated(output, "P1", 1);
        assertAllocated(output, "P2", 2);
        assertAllocated(output, "P3", 3);
        assertAllocated(output, "P4", 4);

        // P6 should be allocated to Block5
        assertAllocated(output, "P6", 5);

        // P5 has no block large enough → waiting
        AllocationResult p5 = findResult(output, "P5");
        assertFalse(p5.isAllocated());
    }

    @Test
    void testBestFit() {
        List<MemoryBlock> blocks = List.of(
                new MemoryBlock(1, 100),
                new MemoryBlock(2, 500),
                new MemoryBlock(3, 200),
                new MemoryBlock(4, 300)
        );
        String[] pids = {"P1"};
        int[] sizes = {150};

        // Best-Fit: P1 needs 150 → Block3(200) is closest fit (waste=50)
        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.BEST_FIT);
        assertAllocated(output, "P1", 3);
        assertEquals(50, findResult(output, "P1").getInternalFrag());
    }

    @Test
    void testWorstFit() {
        List<MemoryBlock> blocks = List.of(
                new MemoryBlock(1, 100),
                new MemoryBlock(2, 500),
                new MemoryBlock(3, 200),
                new MemoryBlock(4, 300)
        );
        String[] pids = {"P1"};
        int[] sizes = {150};

        // Worst-Fit: P1 needs 150 → Block2(500) is largest (waste=350)
        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.WORST_FIT);
        assertAllocated(output, "P1", 2);
        assertEquals(350, findResult(output, "P1").getInternalFrag());
    }

    @Test
    void testAllProcessesWaiting() {
        List<MemoryBlock> blocks = List.of(new MemoryBlock(1, 10));
        String[] pids = {"P1", "P2"};
        int[] sizes = {50, 60};

        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.FIRST_FIT);
        assertEquals(0, output.getAllocatedCount());
        assertEquals(2, output.getWaitingCount());
    }

    @Test
    void testExactFit_NoInternalFrag() {
        List<MemoryBlock> blocks = List.of(new MemoryBlock(1, 100));
        String[] pids = {"P1"};
        int[] sizes = {100};

        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.FIRST_FIT);
        assertAllocated(output, "P1", 1);
        assertEquals(0, findResult(output, "P1").getInternalFrag());
        assertEquals(0, output.getTotalInternalFrag());
    }

    @Test
    void testExternalFragCalculation() {
        List<MemoryBlock> blocks = List.of(
                new MemoryBlock(1, 100),
                new MemoryBlock(2, 50),
                new MemoryBlock(3, 200)
        );
        String[] pids = {"P1"};
        int[] sizes = {80};

        // First-Fit: P1→Block1(100). Block2(50)+Block3(200)=250 free → external frag
        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.FIRST_FIT);
        assertEquals(250, output.getExternalFrag()); // Block2+Block3 unused
    }

    @Test
    void testSingleBlock_SingleProcess() {
        List<MemoryBlock> blocks = List.of(new MemoryBlock(1, 200));
        String[] pids = {"P1"};
        int[] sizes = {120};

        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.FIRST_FIT);
        assertEquals(1, output.getAllocatedCount());
        assertEquals(80, output.getTotalInternalFrag());
        assertEquals(0, output.getExternalFrag());
    }

    @Test
    void testValidation_EmptyBlocks() {
        List<String> errors = engine.validate(List.of(), new int[]{10});
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidation_NegativeProcessSize() {
        List<String> errors = engine.validate(
                List.of(new MemoryBlock(1, 100)), new int[]{-5});
        assertFalse(errors.isEmpty());
    }

    @Test
    void testLargeInput() {
        List<MemoryBlock> blocks = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) blocks.add(new MemoryBlock(i + 1, 100 + i * 10));
        String[] pids = new String[200];
        int[] sizes = new int[200];
        for (int i = 0; i < 200; i++) {
            pids[i] = "P" + (i + 1);
            sizes[i] = 50 + i * 5;
        }

        FragmentationOutput output = engine.allocate(blocks, pids, sizes, Strategy.FIRST_FIT);
        assertEquals(200, output.getTotalProcesses());
        assertTrue(output.getAllocatedCount() > 0);
    }

    private void assertAllocated(FragmentationOutput output, String pid, int expectedBlockId) {
        AllocationResult r = findResult(output, pid);
        assertTrue(r.isAllocated(), pid + " should be allocated");
        assertEquals(expectedBlockId, r.getBlockId(), pid + " block ID");
    }

    private AllocationResult findResult(FragmentationOutput output, String pid) {
        return output.getResults().stream()
                .filter(r -> r.getProcessId().equals(pid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Process " + pid + " not found"));
    }
}
