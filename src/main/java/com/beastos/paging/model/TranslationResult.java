package com.beastos.paging.model;

/**
 * Result of a single logical-to-physical address translation.
 */
public class TranslationResult {
    private final String requestId;
    private final int logicalAddress;
    private final int pageNumber;
    private final int offset;
    private final int frameNumber;   // -1 if page out of bounds
    private final int physicalAddress; // -1 if invalid
    private final boolean valid;
    private final String status;      // "SUCCESS / VALID" or "TRAP: Out of bounds (>N)"

    public TranslationResult(String requestId, int logicalAddress, int pageNumber,
                              int offset, int frameNumber, int physicalAddress,
                              boolean valid, String status) {
        this.requestId = requestId;
        this.logicalAddress = logicalAddress;
        this.pageNumber = pageNumber;
        this.offset = offset;
        this.frameNumber = frameNumber;
        this.physicalAddress = physicalAddress;
        this.valid = valid;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public int getLogicalAddress() { return logicalAddress; }
    public int getPageNumber() { return pageNumber; }
    public int getOffset() { return offset; }
    public int getFrameNumber() { return frameNumber; }
    public int getPhysicalAddress() { return physicalAddress; }
    public boolean isValid() { return valid; }
    public String getStatus() { return status; }

    /**
     * Display string for the logical address, e.g. "14348 (Page 3)" or "Page 5, Offset 600"
     */
    public String getLogicalAddressDisplay() {
        if (logicalAddress < 0) {
            return "Page " + pageNumber + ", Offset " + offset;
        }
        return logicalAddress + " (Page " + pageNumber + ", Off " + offset + ")";
    }

    /**
     * Frame display, e.g. "Frame 3" or "Frame -"
     */
    public String getFrameDisplay() {
        return valid ? "Frame " + frameNumber : "Frame -";
    }
}
