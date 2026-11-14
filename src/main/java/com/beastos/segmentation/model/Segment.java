package com.beastos.segmentation.model;

/**
 * Represents a single segment in the segment table.
 */
public class Segment {
    private final int segmentNumber;
    private final int base;
    private final int limit;

    public Segment(int segmentNumber, int base, int limit) {
        this.segmentNumber = segmentNumber;
        this.base = base;
        this.limit = limit;
    }

    public int getSegmentNumber() { return segmentNumber; }
    public int getBase() { return base; }
    public int getLimit() { return limit; }

    @Override
    public String toString() {
        return "Seg " + segmentNumber + " [Base " + base + ", Limit " + limit + "]";
    }
}
