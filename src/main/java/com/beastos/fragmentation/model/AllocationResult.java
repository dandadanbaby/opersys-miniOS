package com.beastos.fragmentation.model;

/**
 * Result of allocating a single process to a memory block.
 */
public class AllocationResult {
    private final String processId;
    private final int processSize;
    private final int blockId;       // -1 if not allocated (waiting)
    private final int blockSize;     // original block size, 0 if waiting
    private final boolean allocated;
    private final int internalFrag;  // blockSize - processSize, 0 if waiting
    private final String status;     // "Allocated" or "Waiting"

    public AllocationResult(String processId, int processSize,
                             int blockId, int blockSize, boolean allocated) {
        this.processId = processId;
        this.processSize = processSize;
        this.blockId = blockId;
        this.blockSize = blockSize;
        this.allocated = allocated;
        this.internalFrag = allocated ? blockSize - processSize : 0;
        this.status = allocated ? "Allocated" : "Waiting";
    }

    public String getProcessId() { return processId; }
    public int getProcessSize() { return processSize; }
    public int getBlockId() { return blockId; }
    public int getBlockSize() { return blockSize; }
    public boolean isAllocated() { return allocated; }
    public int getInternalFrag() { return internalFrag; }
    public String getStatus() { return status; }

    public String getBlockDisplay() {
        return allocated ? "Block " + blockId + " (" + blockSize + " KB)" : "None";
    }
}
