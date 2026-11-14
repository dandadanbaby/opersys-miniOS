package com.beastos.fragmentation.model;

/**
 * A memory block (partition) in the fragmentation simulation.
 */
public class MemoryBlock {
    private final int blockId;
    private final int size;

    public MemoryBlock(int blockId, int size) {
        this.blockId = blockId;
        this.size = size;
    }

    public int getBlockId() { return blockId; }
    public int getSize() { return size; }

    @Override
    public String toString() {
        return "Block " + blockId + " (" + size + " KB)";
    }
}
