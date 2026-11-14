package com.beastos.segmentation;

import com.beastos.segmentation.engine.SegmentationEngine;
import com.beastos.segmentation.model.Segment;
import com.beastos.segmentation.model.SegmentTranslationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Segmentation engine.
 */
class SegmentationEngineTest {

    private SegmentationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SegmentationEngine();
    }

    @Test
    void testDesignReferenceCase() {
        // From design: Seg 1, Base=6300, Limit=400, Offset=320
        // Physical = 6300 + 320 = 6620
        List<Segment> segments = List.of(
                new Segment(0, 1400, 100),
                new Segment(1, 6300, 400),
                new Segment(2, 4300, 500),
                new Segment(3, 2000, 250),
                new Segment(4, 7200, 150)
        );

        SegmentTranslationResult result = engine.translate(segments, 1, 320);
        assertTrue(result.isValid());
        assertEquals(6620, result.getPhysicalAddress());
        assertEquals(6300, result.getBase());
        assertEquals(400, result.getLimit());
    }

    @Test
    void testOffsetExceedsLimit_SegFault() {
        List<Segment> segments = List.of(
                new Segment(0, 1400, 100),
                new Segment(1, 6300, 400)
        );

        // Offset 450 >= limit 400 → SEGMENTATION FAULT
        SegmentTranslationResult result = engine.translate(segments, 1, 450);
        assertFalse(result.isValid());
        assertTrue(result.getStatus().contains("SEGMENTATION FAULT"));
    }

    @Test
    void testOffsetExactlyAtLimit() {
        List<Segment> segments = List.of(new Segment(0, 1000, 100));

        // Offset 100 >= limit 100 → FAULT (offset must be < limit)
        SegmentTranslationResult result = engine.translate(segments, 0, 100);
        assertFalse(result.isValid());
    }

    @Test
    void testOffsetZero() {
        List<Segment> segments = List.of(new Segment(0, 5000, 200));
        SegmentTranslationResult result = engine.translate(segments, 0, 0);
        assertTrue(result.isValid());
        assertEquals(5000, result.getPhysicalAddress());
    }

    @Test
    void testSegmentOutOfBounds() {
        List<Segment> segments = List.of(new Segment(0, 1000, 100));
        SegmentTranslationResult result = engine.translate(segments, 5, 10);
        assertFalse(result.isValid());
        assertTrue(result.getStatus().contains("does not exist"));
    }

    @Test
    void testNegativeSegment() {
        List<Segment> segments = List.of(new Segment(0, 1000, 100));
        SegmentTranslationResult result = engine.translate(segments, -1, 10);
        assertFalse(result.isValid());
    }

    @Test
    void testBatchTranslation() {
        List<Segment> segments = List.of(
                new Segment(0, 1400, 100),
                new Segment(1, 6300, 400)
        );
        int[][] requests = {{1, 320}, {1, 10}, {0, 50}, {1, 450}};
        List<SegmentTranslationResult> results = engine.translateAll(segments, requests);

        assertEquals(4, results.size());
        assertTrue(results.get(0).isValid());  // 6300+320=6620
        assertTrue(results.get(1).isValid());  // 6300+10=6310
        assertTrue(results.get(2).isValid());  // 1400+50=1450
        assertFalse(results.get(3).isValid()); // 450 >= 400 → fault
    }

    @Test
    void testValidation_EmptySegments() {
        List<String> errors = engine.validate(List.of());
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidation_NegativeBase() {
        List<String> errors = engine.validate(List.of(new Segment(0, -100, 50)));
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidation_ZeroLimit() {
        List<String> errors = engine.validate(List.of(new Segment(0, 100, 0)));
        assertFalse(errors.isEmpty());
    }
}
