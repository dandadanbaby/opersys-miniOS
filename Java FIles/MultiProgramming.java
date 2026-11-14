
public class MultiProgramming {

    public static void run() {
        Integer totalMem = OSUtils.getInt("Enter Total System Memory in MB (>= 1):", 1);
        if (totalMem == null) {
            return;
        }

        Integer n = OSUtils.getInt("Enter number of Processes to load (>= 1):", 1);
        if (n == null) {
            return;
        }

        int[] memReq = OSUtils.getIntArray("Enter Memory required (in MB) for each of the " + n + " Processes (space-separated):", n, 1);
        if (memReq == null) {
            return;
        }

        int usedMem = 0, activeProcs = 0;
        StringBuilder out = new StringBuilder("MULTIPROGRAMMING SYSTEM RESOURCE UTILIZATION\n");
        out.append("Total System Memory: ").append(totalMem).append(" MB\n\n");
        out.append(String.format("%-10s %-18s %-20s %-18s\n", "Process", "Memory Needed", "Status", "Remaining RAM"));
        out.append("--------------------------------------------------------------------\n");

        for (int i = 0; i < n; i++) {
            if (usedMem + memReq[i] <= totalMem) {
                usedMem += memReq[i];
                activeProcs++;
                out.append(String.format("P%-9d %-18s %-20s %-18s\n",
                        (i + 1), memReq[i] + " MB", "Active in Memory", (totalMem - usedMem) + " MB"));
            } else {
                out.append(String.format("P%-9d %-18s %-20s %-18s\n",
                        (i + 1), memReq[i] + " MB", "Waiting (RAM Full)", (totalMem - usedMem) + " MB"));
            }
        }

        double utilization = (usedMem * 100.0) / totalMem;
        out.append("--------------------------------------------------------------------\n");
        out.append("Degree of Multiprogramming (Active Processes): ").append(activeProcs).append(" / ").append(n).append("\n");
        out.append("Total Memory Allocated:                        ").append(usedMem).append(" MB\n");
        out.append("Free / Unused Memory:                          ").append(totalMem - usedMem).append(" MB\n");
        out.append(String.format("Memory Utilization:                            %.2f%%", utilization));

        OSUtils.showOutput("Multiprogramming Simulation", out.toString());
    }
}
