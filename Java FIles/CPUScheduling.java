
import java.util.*;
import javax.swing.JOptionPane;

public class CPUScheduling {

    public static void run() {
        String[] algos = {"FCFS (First-Come, First-Served)", "SJF (Shortest Job First)", "Priority Scheduling", "Round Robin (RR)"};
        String algo = (String) JOptionPane.showInputDialog(null, "Select CPU Scheduling Algorithm:",
                "CPU Scheduling", JOptionPane.PLAIN_MESSAGE, null, algos, algos[0]);
        if (algo == null) {
            return;
        }

        Integer n = OSUtils.getInt("Enter number of processes (1 or more):", 1);
        if (n == null) {
            return;
        }

        int[] pid = new int[n];
        int[] at = new int[n];
        int[] bt = new int[n];
        int[] prio = new int[n];

        for (int i = 0; i < n; i++) {
            pid[i] = i + 1;
            Integer a = OSUtils.getInt("Enter Arrival Time for P" + pid[i] + " (>= 0):", 0);
            if (a == null) {
                return;
            }
            at[i] = a;

            Integer b = OSUtils.getInt("Enter Burst Time for P" + pid[i] + " (>= 1):", 1);
            if (b == null) {
                return;
            }
            bt[i] = b;

            if (algo.startsWith("Priority")) {
                Integer p = OSUtils.getInt("Enter Priority for P" + pid[i] + " (Lower number = Higher priority, >= 1):", 1);
                if (p == null) {
                    return;
                }
                prio[i] = p;
            }
        }

        int quantum = 1;
        if (algo.startsWith("Round Robin")) {
            Integer q = OSUtils.getInt("Enter Time Quantum (>= 1):", 1);
            if (q == null) {
                return;
            }
            quantum = q;
        }

        int[] ct = new int[n];
        int[] tat = new int[n];
        int[] wt = new int[n];
        StringBuilder order = new StringBuilder();

        if (algo.startsWith("FCFS")) {
            Integer[] idx = new Integer[n];
            for (int i = 0; i < n; i++) {
                idx[i] = i;
            }
            Arrays.sort(idx, Comparator.comparingInt(i -> at[i]));

            int time = 0;
            for (int i : idx) {
                if (time < at[i]) {
                    time = at[i];
                }
                time += bt[i];
                ct[i] = time;
                order.append("P").append(pid[i]).append(" -> ");
            }
        } else if (algo.startsWith("SJF") || algo.startsWith("Priority")) {
            boolean[] done = new boolean[n];
            int completed = 0, time = 0;
            while (completed < n) {
                int best = -1;
                for (int i = 0; i < n; i++) {
                    if (!done[i] && at[i] <= time) {
                        if (best == -1) {
                            best = i;
                        } else if (algo.startsWith("SJF") && bt[i] < bt[best]) {
                            best = i;
                        } else if (algo.startsWith("Priority") && prio[i] < prio[best]) {
                            best = i;
                        }
                    }
                }
                if (best == -1) {
                    time++;
                } else {
                    time += bt[best];
                    ct[best] = time;
                    done[best] = true;
                    completed++;
                    order.append("P").append(pid[best]).append(" -> ");
                }
            }
        } else if (algo.startsWith("Round Robin")) {
            int[] rem = Arrays.copyOf(bt, n);
            boolean[] inQueue = new boolean[n];
            Queue<Integer> q = new LinkedList<>();
            int time = 0, completed = 0;

            Integer[] idx = new Integer[n];
            for (int i = 0; i < n; i++) {
                idx[i] = i;
            }
            Arrays.sort(idx, Comparator.comparingInt(i -> at[i]));

            time = at[idx[0]];
            for (int i : idx) {
                if (at[i] <= time) {
                    q.add(i);
                    inQueue[i] = true;
                }
            }

            while (completed < n) {
                if (q.isEmpty()) {
                    time++;
                    for (int i : idx) {
                        if (!inQueue[i] && rem[i] > 0 && at[i] <= time) {
                            q.add(i);
                            inQueue[i] = true;
                        }
                    }
                    continue;
                }
                int curr = q.poll();
                order.append("P").append(pid[curr]).append(" -> ");
                int exec = Math.min(quantum, rem[curr]);
                rem[curr] -= exec;
                time += exec;

                for (int i : idx) {
                    if (!inQueue[i] && rem[i] > 0 && at[i] <= time) {
                        q.add(i);
                        inQueue[i] = true;
                    }
                }

                if (rem[curr] > 0) {
                    q.add(curr);
                } else {
                    ct[curr] = time;
                    completed++;
                }
            }
        }

        double totalWT = 0, totalTAT = 0;
        StringBuilder out = new StringBuilder("Algorithm: " + algo + "\n");
        out.append("Scheduling Order: ").append(order.substring(0, order.length() - 4)).append("\n\n");
        out.append(String.format("%-8s %-10s %-10s %-10s %-12s %-12s %-10s\n",
                "Process", "Arrival", "Burst", "Priority", "Completion", "Turnaround", "Waiting"));
        out.append("------------------------------------------------------------------------------\n");

        for (int i = 0; i < n; i++) {
            tat[i] = ct[i] - at[i];
            wt[i] = tat[i] - bt[i];
            totalWT += wt[i];
            totalTAT += tat[i];
            out.append(String.format("P%-7d %-10d %-10d %-10s %-12d %-12d %-10d\n",
                    pid[i], at[i], bt[i], (algo.startsWith("Priority") ? prio[i] : "-"), ct[i], tat[i], wt[i]));
        }

        out.append("------------------------------------------------------------------------------\n");
        out.append(String.format("Average Waiting Time:    %.2f\n", totalWT / n));
        out.append(String.format("Average Turnaround Time: %.2f\n", totalTAT / n));

        OSUtils.showOutput("CPU Scheduling Results", out.toString());
    }
}
