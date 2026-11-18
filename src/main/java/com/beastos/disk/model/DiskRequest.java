package com.beastos.disk.model;

// simple model class we made for a single disk I/O request
// we store the cylinder number and its original position in the queue so we can track
// arrival order even after algorithms like SSTF or SCAN sort them around
public class DiskRequest {
    private final int index;       // original arrival position in our request queue
    private final int cylinder;    // cylinder (track) number the OS needs to service

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
