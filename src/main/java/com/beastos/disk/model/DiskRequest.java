package com.beastos.disk.model;

/**
 * Represents a single disk I/O request (a cylinder number to be serviced).
 */
public class DiskRequest {
    private final int index;       // original position in the queue
    private final int cylinder;    // cylinder number requested

    public DiskRequest(int index, int cylinder) {
        this.index = index;
        this.cylinder = cylinder;
    }

    public int getIndex() { return index; }
    public int getCylinder() { return cylinder; }

    @Override
    public String toString() {
        return "Request[" + index + " → Cyl " + cylinder + "]";
    }
}
