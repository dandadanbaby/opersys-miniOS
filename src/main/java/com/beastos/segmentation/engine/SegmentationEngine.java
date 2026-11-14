package com.beastos.segmentation.engine;

import com.beastos.segmentation.model.Segment;
import com.beastos.segmentation.model.SegmentTranslationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Segmentation engine: translates logical addresses (segment, offset)
 * to physical addresses using a segment table.
 *
 * Formula: if offset < limit → Physical = Base + Offset
 *          else → SEGMENTATION FAULT (trap)
 *
 * Pure Java — no JavaFX dependency.
 */
public class SegmentationEngine {

    /**
     * Translate a logical address given a segment table.
     *
     * @param segments list of segments in the segment table
     * @param segmentNumber the segment being accessed
     * @param offset the offset within the segment
     * @return translation result
     */
    public SegmentTranslationResult translate(List<Segment> segments, int segmentNumber, int offset) {
        if (segmentNumber < 0 || segmentNumber >= segments.size()) {
            return new SegmentTranslationResult(segmentNumber, offset, -1, -1, -1, false,
                    "TRAP: Segment " + segmentNumber + " does not exist (out of bounds).");
        }

        Segment seg = segments.get(segmentNumber);

        if (offset < 0) {
            return new SegmentTranslationResult(segmentNumber, offset, seg.getBase(), seg.getLimit(),
                    -1, false, "TRAP: Negative offset.");
        }

        if (offset >= seg.getLimit()) {
            return new SegmentTranslationResult(segmentNumber, offset, seg.getBase(), seg.getLimit(),
                    -1, false,
                    "SEGMENTATION FAULT (Trap): Offset " + offset + " exceeds limit (" + seg.getLimit() + ").");
        }

        int physical = seg.getBase() + offset;
        return new SegmentTranslationResult(segmentNumber, offset, seg.getBase(), seg.getLimit(),
                physical, true, "SUCCESS");
    }

    /**
     * Batch translate multiple (segment, offset) pairs.
     */
    public List<SegmentTranslationResult> translateAll(List<Segment> segments, int[][] requests) {
        List<SegmentTranslationResult> results = new ArrayList<>();
        for (int[] req : requests) {
            results.add(translate(segments, req[0], req[1]));
        }
        return results;
    }

    /**
     * Validate segment table entries.
     */
    public List<String> validate(List<Segment> segments) {
        List<String> errors = new ArrayList<>();
        if (segments == null || segments.isEmpty()) {
            errors.add("Segment table must have at least one entry.");
            return errors;
        }
        for (Segment s : segments) {
            if (s.getBase() < 0) errors.add("Seg " + s.getSegmentNumber() + ": Base cannot be negative.");
            if (s.getLimit() <= 0) errors.add("Seg " + s.getSegmentNumber() + ": Limit must be positive.");
        }
        return errors;
    }
}
