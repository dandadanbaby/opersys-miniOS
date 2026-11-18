package com.beastos.banker.model;

import java.util.Arrays;

/*
 * this class holds the complete snapshot of the system for the Banker's Algorithm.
 * our prof explained that we don't need the user to input the Need matrix because
 * it's always just Need = Max - Allocation, so we calculate it automatically here!
 */
public class BankerState {
    private final int processes;
    private final int resources;
    private final int[][] allocation;
    private final int[][] maximum;
    private final int[][] need;
    private final int[] available;
    private final int[] totalResources;

    public BankerState(int[][] allocation, int[][] maximum, int[] available) {
        this.processes = allocation.length;
        this.resources = allocation[0].length;
        // we deep copy these so whatever passes them in can't accidentally mutate our state
        this.allocation = deepCopy(allocation);
        this.maximum = deepCopy(maximum);
        this.available = Arrays.copyOf(available, available.length);

        // basically what happens here is: Need = Max - Allocation
        // this loop calculates how many more resource units each process might still request before finishing
        this.need = new int[processes][resources];
        for (int i = 0; i < processes; i++) {
            for (int j = 0; j < resources; j++) {
                need[i][j] = maximum[i][j] - allocation[i][j];
            }
        }

        // we needed this to figure out the total resource instances existing in the whole system
        // total = currently available + what all processes are currently holding
        this.totalResources = new int[resources];
        for (int j = 0; j < resources; j++) {
            totalResources[j] = available[j];
            for (int i = 0; i < processes; i++) {
                totalResources[j] += allocation[i][j];
            }
        }
    }

    public int getProcesses() { return processes; }
    public int getResources() { return resources; }
    public int[][] getAllocation() { return deepCopy(allocation); }
    public int[][] getMaximum() { return deepCopy(maximum); }
    public int[][] getNeed() { return deepCopy(need); }
    public int[] getAvailable() { return Arrays.copyOf(available, available.length); }
    public int[] getTotalResources() { return Arrays.copyOf(totalResources, totalResources.length); }

    public int getAllocation(int p, int r) { return allocation[p][r]; }
    public int getMaximum(int p, int r) { return maximum[p][r]; }
    public int getNeed(int p, int r) { return need[p][r]; }
    public int getAvailable(int r) { return available[r]; }

    // this part is tricky because a normal clone() or copyOf() on a 2D array only copies row references!
    // so we wrote this helper to clone every inner row array so we don't accidentally share references
    private static int[][] deepCopy(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }
}
