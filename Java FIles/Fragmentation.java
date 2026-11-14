
import java.util.Arrays;

public class Fragmentation {

    public static void run() {
        Integer m = OSUtils.getInt("Enter number of Memory Blocks (>= 1):", 1);
        if (m == null) {
            return;
        }
        int[] blocks = OSUtils.getIntArray("Enter sizes of the " + m + " Memory Blocks in KB (space-separated):", m, 1);
        if (blocks == null) {
            return;
        }

        Integer n = OSUtils.getInt("Enter number of Processes (>= 1):", 1);
        if (n == null) {
            return;
        }
        int[] procs = OSUtils.getIntArray("Enter memory required for the " + n + " Processes in KB (space-separated):", n, 1);
        if (procs == null) {
            return;
        }

        boolean[] blockUsed = new boolean[m];
        int totalInternalFrag = 0;

        StringBuilder out = new StringBuilder("MEMORY ALLOCATION & FRAGMENTATION (First-Fit)\n");
        out.append("Initial Memory Blocks (KB): ").append(Arrays.toString(blocks)).append("\n\n");
        out.append(String.format("%-10s %-14s %-15s %-15s %-18s\n", "Process", "Process Size", "Status", "Block Used", "Internal Frag"));
        out.append("----------------------------------------------------------------------------\n");

        int unallocatedDemand = 0;
        for (int i = 0; i < n; i++) {
            int allocatedBlock = -1;
            for (int j = 0; j < m; j++) {
                if (!blockUsed[j] && blocks[j] >= procs[i]) {
                    allocatedBlock = j;
                    blockUsed[j] = true;
                    break;
                }
            }
            if (allocatedBlock != -1) {
                int internalFrag = blocks[allocatedBlock] - procs[i];
                totalInternalFrag += internalFrag;
                out.append(String.format("P%-9d %-14s %-15s Block %-9d %-18s\n",
                        (i + 1), procs[i] + " KB", "Allocated", (allocatedBlock + 1), internalFrag + " KB"));
            } else {
                unallocatedDemand += procs[i];
                out.append(String.format("P%-9d %-14s %-15s %-15s %-18s\n",
                        (i + 1), procs[i] + " KB", "Not Allocated", "None", "N/A"));
            }
        }

        int freeBlocksMemory = 0;
        for (int j = 0; j < m; j++) {
            if (!blockUsed[j]) {
                freeBlocksMemory += blocks[j];
            }
        }

        out.append("----------------------------------------------------------------------------\n");
        out.append("Total Internal Fragmentation (inside allocated blocks): ").append(totalInternalFrag).append(" KB\n");
        out.append("Total Unused Memory in Free Blocks:                     ").append(freeBlocksMemory).append(" KB\n");
        if (unallocatedDemand > 0 && freeBlocksMemory > 0) {
            out.append("External Fragmentation Status: ").append(freeBlocksMemory)
                    .append(" KB is free across unused blocks, but unallocated processes could not fit.");
        } else {
            out.append("External Fragmentation Status: None.");
        }

        OSUtils.showOutput("Fragmentation Simulation", out.toString());
    }
}
