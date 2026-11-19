package com.beastos.paging;

import com.beastos.paging.engine.PagingEngine;
import com.beastos.paging.model.PagingOutput;
import com.beastos.paging.model.TranslationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Paging engine: page table lookup and address translation.
 */
class PagingEngineTest {

    private PagingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PagingEngine();
    }

    @Test
    void testDesignReferenceCase() {
        // From the BEAST OS design reference:
        // Page size = 4096, 8 pages: P0→F5, P1→F2, P2→F9, P3→F3, P4→F12, P5→F15, P6→F0, P7→F7
        int pageSize = 4096;
        int[] pageTable = {5, 2, 9, 3, 12, 15, 0, 7};

        // Logical address 14348 → Page 3, Offset 2060 → Frame 3 → Physical 3*4096+2060 = 14348
        int[] addresses = {14348};
        PagingOutput output = engine.translate(pageSize, pageTable, addresses);

        assertEquals(1, output.getTranslationCount());
        TranslationResult t = output.getTranslations().get(0);

        assertTrue(t.isValid());
        assertEquals(3, t.getPageNumber());    // 14348 / 4096 = 3
        assertEquals(2060, t.getOffset());     // 14348 % 4096 = 2060
        assertEquals(3, t.getFrameNumber());   // pageTable[3] = 3
        assertEquals(14348, t.getPhysicalAddress()); // 3 * 4096 + 2060 = 14348
    }

    @Test
    void testMultipleTranslations() {
        int pageSize = 4096;
        int[] pageTable = {5, 2, 9, 3, 12, 15, 0, 7};

        // From design: P1(14348), P2(22440), P3(6200), P4(45056)
        int[] addresses = {14348, 22440, 6200, 45056};
        PagingOutput output = engine.translate(pageSize, pageTable, addresses);

        assertEquals(4, output.getTranslationCount());

        // P1: 14348 → Page 3, Offset 2060, Frame 3 → 14348
        TranslationResult t1 = output.getTranslations().get(0);
        assertTrue(t1.isValid());
        assertEquals(3, t1.getPageNumber());
        assertEquals(14348, t1.getPhysicalAddress());

        // P2: 22440 → Page 5, Offset 1960, Frame 15 → 15*4096+1960 = 63400
        TranslationResult t2 = output.getTranslations().get(1);
        assertTrue(t2.isValid());
        assertEquals(5, t2.getPageNumber());
        assertEquals(15, t2.getFrameNumber());

        // P3: 6200 → Page 1, Offset 2104, Frame 2 → 2*4096+2104 = 10296
        TranslationResult t3 = output.getTranslations().get(2);
        assertTrue(t3.isValid());
        assertEquals(1, t3.getPageNumber());
        assertEquals(2, t3.getFrameNumber());

        // P4: 45056 → Page 11 → out of bounds (>7)
        TranslationResult t4 = output.getTranslations().get(3);
        assertFalse(t4.isValid());
        assertEquals(11, t4.getPageNumber());
        assertTrue(t4.getStatus().contains("TRAP"));
    }

    @Test
    void testPageZeroTranslation() {
        int pageSize = 1024;
        int[] pageTable = {10, 20, 30};

        int[] addresses = {0};
        PagingOutput output = engine.translate(pageSize, pageTable, addresses);

        TranslationResult t = output.getTranslations().get(0);
        assertTrue(t.isValid());
        assertEquals(0, t.getPageNumber());
        assertEquals(0, t.getOffset());
        assertEquals(10, t.getFrameNumber());
        assertEquals(10240, t.getPhysicalAddress()); // 10 * 1024 = 10240
    }

    @Test
    void testOutOfBoundsPage() {
        int pageSize = 100;
        int[] pageTable = {5, 10};  // Only pages 0 and 1

        int[] addresses = {250};  // Page 2 → out of bounds
        PagingOutput output = engine.translate(pageSize, pageTable, addresses);

        TranslationResult t = output.getTranslations().get(0);
        assertFalse(t.isValid());
        assertEquals(2, t.getPageNumber());
        assertTrue(t.getStatus().contains("Out of bounds"));
    }

    @Test
    void testValidation_NegativePageSize() {
        List<String> errors = engine.validate(-1, new int[]{1, 2});
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("positive"));
    }

    @Test
    void testValidation_EmptyPageTable() {
        List<String> errors = engine.validate(100, new int[]{});
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidation_NegativeFrame() {
        List<String> errors = engine.validate(100, new int[]{1, -1, 3});
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("negative"));
    }

    @Test
    void testLargePageTable() {
        int pageSize = 4096;
        int[] pageTable = new int[64];
        for (int i = 0; i < 64; i++) pageTable[i] = i * 2;

        int[] addresses = new int[100];
        for (int i = 0; i < 100; i++) addresses[i] = i * 1000;

        PagingOutput output = engine.translate(pageSize, pageTable, addresses);
        assertEquals(100, output.getTranslationCount());
        // Some should be valid (page < 64), some out of bounds
        assertTrue(output.getValidCount() > 0);
    }

    @Test
    void testPageOffsetRequestAndTrap() {
        int pageSize = 512;
        int[] pageTable = {14, 3, 9, 21, 7, 12, 0, 5};

        List<PagingEngine.PagingRequest> requests = List.of(
                new PagingEngine.PagingRequest(5, 120), // Valid
                new PagingEngine.PagingRequest(5, 600)  // Offset 600 >= 512 -> Trap
        );

        PagingOutput output = engine.translateRequests(pageSize, pageTable, requests);
        assertEquals(2, output.getTranslationCount());

        TranslationResult valid = output.getTranslations().get(0);
        assertTrue(valid.isValid());
        assertEquals(5, valid.getPageNumber());
        assertEquals(120, valid.getOffset());
        assertEquals(12, valid.getFrameNumber());
        assertEquals(12 * 512 + 120, valid.getPhysicalAddress()); // 6264

        TranslationResult trap = output.getTranslations().get(1);
        assertFalse(trap.isValid());
        assertEquals(5, trap.getPageNumber());
        assertEquals(600, trap.getOffset());
        assertTrue(trap.getStatus().contains("TRAP"));
        assertTrue(trap.getStatus().contains("exceeds Page Size"));
    }

    @Test
    void testSinglePageSingleAddress() {
        int[] pageTable = {42};
        int[] addresses = {50};
        PagingOutput output = engine.translate(100, pageTable, addresses);

        TranslationResult t = output.getTranslations().get(0);
        assertTrue(t.isValid());
        assertEquals(0, t.getPageNumber());  // 50/100=0
        assertEquals(50, t.getOffset());     // 50%100=50
        assertEquals(42, t.getFrameNumber());
        assertEquals(42 * 100 + 50, t.getPhysicalAddress());
    }
}
