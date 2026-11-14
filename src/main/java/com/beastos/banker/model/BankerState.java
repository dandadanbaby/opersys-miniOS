package com.beastos.banker.model;

import java.util.Arrays;

/**
 * Represents the complete state of a Banker's Algorithm configuration.
 * Need is always auto-calculated: Need = Max - Allocation.
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
        this.allocation = deepCopy(allocation);
        this.maximum = deepCopy(maximum);
        this.available = Arrays.copyOf(available, available.length);

        // Calculate Need = Max - Allocation
        this.need = new int[processes][resources];
        for (int i = 0; i < processes; i++) {
            for (int j = 0; j < resources; j++) {
                need[i][j] = maximum[i][j] - allocation[i][j];
            }
        }

        // Calculate total resources = sum of all allocated + available
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

    private static int[][] deepCopy(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }
}
