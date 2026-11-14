
import java.util.*;
import javax.swing.JOptionPane;

public class PageReplacement {

    public static void run() {
        String[] algos = {"FIFO (First-In, First-Out)", "LRU (Least Recently Used)", "Optimal Page Replacement"};
        String algo = (String) JOptionPane.showInputDialog(null, "Select Page Replacement Algorithm:",
                "Page Replacement", JOptionPane.PLAIN_MESSAGE, null, algos, algos[0]);
        if (algo == null) {
            return;
        }

        Integer framesCount = OSUtils.getInt("Enter number of Frames (>= 1):", 1);
        if (framesCount == null) {
            return;
        }

        int[] pages = OSUtils.getIntArray("Enter Page Reference String (space-separated, e.g., 7 0 1 2 0 3 0 4):", -1, 0);
        if (pages == null) {
            return;
        }

        int[] frames = new int[framesCount];
        Arrays.fill(frames, -1);
        int faults = 0, hits = 0, fifoPtr = 0;

        StringBuilder out = new StringBuilder("Algorithm: " + algo + "\n");
        out.append(String.format("%-10s %-30s %-10s\n", "Page", "Frames State", "Status"));
        out.append("------------------------------------------------------\n");

        for (int i = 0; i < pages.length; i++) {
            int p = pages[i];
            boolean isHit = false;
            for (int f : frames) {
                if (f == p) {
                    isHit = true;
                    break;
                }
            }

            if (isHit) {
                hits++;
                out.append(String.format("%-10d %-30s %-10s\n", p, Arrays.toString(frames), "HIT"));
            } else {
                faults++;
                int replaceIdx = -1;

                // Check for empty frame first
                for (int j = 0; j < framesCount; j++) {
                    if (frames[j] == -1) {
                        replaceIdx = j;
                        break;
                    }
                }

                if (replaceIdx == -1) {
                    if (algo.startsWith("FIFO")) {
                        replaceIdx = fifoPtr;
                        fifoPtr = (fifoPtr + 1) % framesCount;
                    } else if (algo.startsWith("LRU")) {
                        int leastRecent = Integer.MAX_VALUE;
                        for (int j = 0; j < framesCount; j++) {
                            int lastSeen = -1;
                            for (int k = i - 1; k >= 0; k--) {
                                if (pages[k] == frames[j]) {
                                    lastSeen = k;
                                    break;
                                }
                            }
                            if (lastSeen < leastRecent) {
                                leastRecent = lastSeen;
                                replaceIdx = j;
                            }
                        }
                    } else if (algo.startsWith("Optimal")) {
                        int farthest = -1;
                        for (int j = 0; j < framesCount; j++) {
                            int nextUse = Integer.MAX_VALUE;
                            for (int k = i + 1; k < pages.length; k++) {
                                if (pages[k] == frames[j]) {
                                    nextUse = k;
                                    break;
                                }
                            }
                            if (nextUse > farthest) {
                                farthest = nextUse;
                                replaceIdx = j;
                            }
                        }
                    }
                } else if (algo.startsWith("FIFO")) {
                    fifoPtr = (fifoPtr + 1) % framesCount;
                }

                frames[replaceIdx] = p;
                out.append(String.format("%-10d %-30s %-10s\n", p, Arrays.toString(frames), "FAULT"));
            }
        }

        out.append("------------------------------------------------------\n");
        out.append("Total Requests:    ").append(pages.length).append("\n");
        out.append("Total Page Faults: ").append(faults).append("\n");
        out.append("Total Page Hits:   ").append(hits).append("\n");
        out.append(String.format("Hit Ratio:         %.2f%%", (hits * 100.0) / pages.length));

        OSUtils.showOutput("Page Replacement Results", out.toString());
    }
}
