
public class Segmentation {

    public static void run() {
        Integer n = OSUtils.getInt("Enter number of Segments (>= 1):", 1);
        if (n == null) {
            return;
        }

        int[] base = new int[n];
        int[] limit = new int[n];

        for (int i = 0; i < n; i++) {
            Integer b = OSUtils.getInt("Enter Base Address for Segment " + i + " (>= 0):", 0);
            if (b == null) {
                return;
            }
            base[i] = b;

            Integer l = OSUtils.getInt("Enter Limit (Length) for Segment " + i + " (>= 1):", 1);
            if (l == null) {
                return;
            }
            limit[i] = l;
        }

        Integer seg = OSUtils.getInt("Enter Logical Segment Number to translate (>= 0):", 0);
        if (seg == null) {
            return;
        }

        Integer offset = OSUtils.getInt("Enter Logical Offset (>= 0):", 0);
        if (offset == null) {
            return;
        }

        StringBuilder out = new StringBuilder("SEGMENT TABLE\n");
        out.append(String.format("%-12s %-15s %-15s\n", "Segment", "Base Address", "Limit"));
        out.append("------------------------------------------\n");
        for (int i = 0; i < n; i++) {
            out.append(String.format("%-12d %-15d %-15d\n", i, base[i], limit[i]));
        }
        out.append("------------------------------------------\n\n");

        if (seg >= n) {
            out.append("ERROR: Invalid Segment Number ").append(seg).append(" (Max Segment is ").append(n - 1).append(").");
        } else if (offset >= limit[seg]) {
            out.append("SEGMENTATION FAULT (Trap): Offset (").append(offset)
                    .append(") exceeds or equals Segment ").append(seg).append(" Limit (").append(limit[seg]).append(").");
        } else {
            int phys = base[seg] + offset;
            out.append("Logical Address:  (Segment ").append(seg).append(", Offset ").append(offset).append(")\n");
            out.append("Validation:       Offset ").append(offset).append(" < Limit ").append(limit[seg]).append(" (VALID)\n");
            out.append("Physical Address: ").append(base[seg]).append(" + ").append(offset).append(" = ").append(phys);
        }

        OSUtils.showOutput("Segmentation Results", out.toString());
    }
}
