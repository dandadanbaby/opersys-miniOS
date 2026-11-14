
import java.util.Arrays;

public class BankersAlgorithm {

    public static void run() {
        Integer n = OSUtils.getInt("Enter number of Processes (N >= 1):", 1);
        if (n == null) {
            return;
        }
        Integer m = OSUtils.getInt("Enter number of Resource Types (M >= 1):", 1);
        if (m == null) {
            return;
        }

        int[][] alloc = new int[n][m];
        int[][] max = new int[n][m];
        int[][] need = new int[n][m];

        for (int i = 0; i < n; i++) {
            int[] a = OSUtils.getIntArray("Enter " + m + " Allocated resources for P" + (i + 1) + " (space-separated):", m, 0);
            if (a == null) {
                return;
            }
            alloc[i] = a;

            while (true) {
                int[] mx = OSUtils.getIntArray("Enter " + m + " Maximum demand resources for P" + (i + 1) + " (space-separated):", m, 0);
                if (mx == null) {
                    return;
                }
                boolean valid = true;
                for (int j = 0; j < m; j++) {
                    if (mx[j] < alloc[i][j]) {
                        OSUtils.showError("Max demand cannot be less than Allocated resources (" + alloc[i][j] + ") at index " + (j + 1) + ".");
                        valid = false;
                        break;
                    }
                    need[i][j] = mx[j] - alloc[i][j];
                }
                if (valid) {
                    max[i] = mx;
                    break;
                }
            }
        }

        int[] avail = OSUtils.getIntArray("Enter " + m + " Available instances for each Resource Type (space-separated):", m, 0);
        if (avail == null) {
            return;
        }

        int[] work = Arrays.copyOf(avail, m);
        boolean[] finish = new boolean[n];
        StringBuilder safeSeq = new StringBuilder();
        int count = 0;

        while (count < n) {
            boolean found = false;
            for (int i = 0; i < n; i++) {
                if (!finish[i]) {
                    boolean canRun = true;
                    for (int j = 0; j < m; j++) {
                        if (need[i][j] > work[j]) {
                            canRun = false;
                            break;
                        }
                    }
                    if (canRun) {
                        for (int j = 0; j < m; j++) {
                            work[j] += alloc[i][j];
                        }
                        finish[i] = true;
                        safeSeq.append("P").append(i + 1).append(" -> ");
                        count++;
                        found = true;
                    }
                }
            }
            if (!found) {
                break;
            }
        }

        StringBuilder out = new StringBuilder("BANKER'S ALGORITHM TABLE\n");
        out.append(String.format("%-10s %-22s %-22s %-22s\n", "Process", "Allocation", "Max", "Need (Max - Alloc)"));
        out.append("----------------------------------------------------------------------------\n");
        for (int i = 0; i < n; i++) {
            out.append(String.format("P%-9d %-22s %-22s %-22s\n",
                    (i + 1), Arrays.toString(alloc[i]), Arrays.toString(max[i]), Arrays.toString(need[i])));
        }
        out.append("----------------------------------------------------------------------------\n");
        out.append("Initial Available Resources: ").append(Arrays.toString(avail)).append("\n\n");

        if (count == n) {
            out.append("STATUS: SYSTEM IS IN A SAFE STATE!\n");
            out.append("Safe Sequence: ").append(safeSeq.substring(0, safeSeq.length() - 4));
        } else {
            out.append("STATUS: SYSTEM IS IN AN UNSAFE STATE (Deadlock Possible)!");
        }

        OSUtils.showOutput("Banker's Algorithm Results", out.toString());
    }
}
