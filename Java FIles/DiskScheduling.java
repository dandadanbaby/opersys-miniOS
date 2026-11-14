
import java.util.*;
import javax.swing.JOptionPane;

public class DiskScheduling {

    public static void run() {
        String[] algos = {"FCFS (First-Come, First-Served)", "SSTF (Shortest Seek Time First)", "SCAN (Moving Towards Max Cylinder)", "C-SCAN (Circular SCAN)"};
        String algo = (String) JOptionPane.showInputDialog(null, "Select Disk Scheduling Algorithm:",
                "Disk Scheduling", JOptionPane.PLAIN_MESSAGE, null, algos, algos[0]);
        if (algo == null) {
            return;
        }

        Integer diskSize = OSUtils.getInt("Enter Maximum Disk Cylinder Limit (e.g., 199):", 1);
        if (diskSize == null) {
            return;
        }

        Integer head = OSUtils.getInt("Enter Initial Disk Head Position (0 to " + diskSize + "):", 0);
        if (head == null) {
            return;
        }
        if (head > diskSize) {
            OSUtils.showError("Initial head position cannot exceed Maximum Disk Cylinder Limit (" + diskSize + ").");
            return;
        }

        int[] reqs = OSUtils.getIntArray("Enter Disk Track Requests (space-separated, e.g., 82 170 43 140 24 16 190):", -1, 0);
        if (reqs == null) {
            return;
        }
        for (int r : reqs) {
            if (r > diskSize) {
                OSUtils.showError("Track request " + r + " exceeds Maximum Disk Cylinder Limit (" + diskSize + ").");
                return;
            }
        }

        List<Integer> seq = new ArrayList<>();
        seq.add(head);
        int totalSeek = 0, curr = head;

        if (algo.startsWith("FCFS")) {
            for (int r : reqs) {
                totalSeek += Math.abs(r - curr);
                curr = r;
                seq.add(curr);
            }
        } else if (algo.startsWith("SSTF")) {
            boolean[] visited = new boolean[reqs.length];
            for (int i = 0; i < reqs.length; i++) {
                int best = -1, minDiff = Integer.MAX_VALUE;
                for (int j = 0; j < reqs.length; j++) {
                    if (!visited[j] && Math.abs(reqs[j] - curr) < minDiff) {
                        minDiff = Math.abs(reqs[j] - curr);
                        best = j;
                    }
                }
                visited[best] = true;
                totalSeek += minDiff;
                curr = reqs[best];
                seq.add(curr);
            }
        } else if (algo.startsWith("SCAN") || algo.startsWith("C-SCAN")) {
            List<Integer> left = new ArrayList<>(), right = new ArrayList<>();
            for (int r : reqs) {
                if (r < head) {
                    left.add(r); 
                }else {
                    right.add(r);
                }
            }
            Collections.sort(left);
            Collections.sort(right);

            for (int r : right) {
                totalSeek += Math.abs(r - curr);
                curr = r;
                seq.add(curr);
            }
            if (!left.isEmpty()) {
                if (curr != diskSize) {
                    totalSeek += Math.abs(diskSize - curr);
                    curr = diskSize;
                    seq.add(curr);
                }
                if (algo.startsWith("SCAN")) {
                    for (int i = left.size() - 1; i >= 0; i--) {
                        totalSeek += Math.abs(left.get(i) - curr);
                        curr = left.get(i);
                        seq.add(curr);
                    }
                } else { // C-SCAN jumps to 0 and services remaining ascending
                    totalSeek += curr; // Jump from diskSize to 0
                    curr = 0;
                    seq.add(curr);
                    for (int r : left) {
                        totalSeek += Math.abs(r - curr);
                        curr = r;
                        seq.add(curr);
                    }
                }
            }
        }

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < seq.size(); i++) {
            path.append(seq.get(i)).append(i < seq.size() - 1 ? " -> " : "");
        }

        StringBuilder out = new StringBuilder("Algorithm: " + algo + "\n");
        out.append("Disk Range: 0 to ").append(diskSize).append(" | Initial Head: ").append(head).append("\n\n");
        out.append("Service Order:\n").append(path).append("\n\n");
        out.append("Total Head Movement: ").append(totalSeek).append(" cylinders");

        OSUtils.showOutput("Disk Scheduling Results", out.toString());
    }
}
