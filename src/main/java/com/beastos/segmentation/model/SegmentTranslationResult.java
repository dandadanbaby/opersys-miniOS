package com.beastos.segmentation.model;

/**
 * Result of a single segment address translation.
 */
public class SegmentTranslationResult {
    private final int segmentNumber;
    private final int offset;
    private final int base;
    private final int limit;
    private final int physicalAddress;  // -1 if trap
    private final boolean valid;
    private final String status;

    public SegmentTranslationResult(int segmentNumber, int offset, int base, int limit,
                                     int physicalAddress, boolean valid, String status) {
        this.segmentNumber = segmentNumber;
        this.offset = offset;
        this.base = base;
        this.limit = limit;
        this.physicalAddress = physicalAddress;
        this.valid = valid;
        this.status = status;
    }

    public int getSegmentNumber() { return segmentNumber; }
    public int getOffset() { return offset; }
    public int getBase() { return base; }
    public int getLimit() { return limit; }
    public int getPhysicalAddress() { return physicalAddress; }
    public boolean isValid() { return valid; }
    public String getStatus() { return status; }

    public String getDescription() {
        if (valid) {
            return "Logical Address (Segment " + segmentNumber + ", Offset " + offset + ")  →  Base "
                    + base + " + Offset " + offset + "  =  Physical Address " + physicalAddress + " (SUCCESS)";
        } else {
            return "SEGMENTATION FAULT (Trap): Offset Exceeds Limit. Attempt to access Seg "
                    + segmentNumber + " at Offset " + offset + " exceeds limit (" + limit + "). Execution halted.";
        }
    }
}
