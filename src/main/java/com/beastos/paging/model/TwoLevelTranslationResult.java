package com.beastos.paging.model;

/**
 * Result of a hierarchical (two-level) logical-to-physical address translation.
 * Format: Logical Address -> [Outer Page p1 | Inner Page p2 | Offset d]
 * Physical Address = (Frame Number * Page Size) + Offset
 */
public class TwoLevelTranslationResult {
    private final String requestId;
    private final int logicalAddress; // -1 if entered as (p1, p2, offset)
    private final int outerPage;      // p1
    private final int innerPage;      // p2
    private final int offset;         // d
    private final int frameNumber;    // -1 if invalid or page fault
    private final int physicalAddress;// -1 if invalid
    private final boolean valid;
    private final String status;      // e.g. "SUCCESS / VALID", "TRAP: Outer Page p1 out of bounds", etc.

    public TwoLevelTranslationResult(String requestId, int logicalAddress, int outerPage,
                                     int innerPage, int offset, int frameNumber,
                                     int physicalAddress, boolean valid, String status) {
        this.requestId = requestId;
        this.logicalAddress = logicalAddress;
        this.outerPage = outerPage;
        this.innerPage = innerPage;
        this.offset = offset;
        this.frameNumber = frameNumber;
        this.physicalAddress = physicalAddress;
        this.valid = valid;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public int getLogicalAddress() { return logicalAddress; }
    public int getOuterPage() { return outerPage; }
    public int getInnerPage() { return innerPage; }
    public int getOffset() { return offset; }
    public int getFrameNumber() { return frameNumber; }
    public int getPhysicalAddress() { return physicalAddress; }
    public boolean isValid() { return valid; }
    public String getStatus() { return status; }

    public String getLogicalAddressDisplay() {
        if (logicalAddress < 0) {
            return "(" + outerPage + ", " + innerPage + ", " + offset + ")";
        }
        return logicalAddress + " (" + outerPage + ", " + innerPage + ", " + offset + ")";
    }

    public String getFrameDisplay() {
        return valid ? "Frame " + frameNumber : "Frame -";
    }
}
