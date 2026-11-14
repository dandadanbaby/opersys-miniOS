
public class Paging {

    public static void run() {
        Integer pageSize = OSUtils.getInt("Enter Page/Frame Size (in bytes, >= 1):", 1);
        if (pageSize == null) {
            return;
        }

        Integer numPages = OSUtils.getInt("Enter total number of Logical Pages (>= 1):", 1);
        if (numPages == null) {
            return;
        }

        int[] pageTable = OSUtils.getIntArray("Enter " + numPages + " Frame Numbers for Pages 0 to " + (numPages - 1) + " (space-separated):", numPages, 0);
        if (pageTable == null) {
            return;
        }

        Integer pageNum = OSUtils.getInt("Enter Logical Page Number to translate (>= 0):", 0);
        if (pageNum == null) {
            return;
        }

        Integer offset = OSUtils.getInt("Enter Offset (>= 0):", 0);
        if (offset == null) {
            return;
        }

        StringBuilder out = new StringBuilder("PAGE TABLE MAPPING (Page Size: " + pageSize + " bytes)\n");
        out.append("Page No.\tFrame No.\n-------------------------\n");
        for (int i = 0; i < numPages; i++) {
            out.append("Page ").append(i).append("\t -> \tFrame ").append(pageTable[i]).append("\n");
        }
        out.append("-------------------------\n\n");

        if (pageNum >= numPages) {
            out.append("ERROR (Trap): Page Number ").append(pageNum).append(" exceeds Page Table limit (Max Page is ").append(numPages - 1).append(").");
        } else if (offset >= pageSize) {
            out.append("ERROR (Trap): Offset ").append(offset).append(" exceeds or equals Page Size (").append(pageSize).append(").");
        } else {
            int frame = pageTable[pageNum];
            int physicalAddr = (frame * pageSize) + offset;
            out.append("Logical Address:  (Page ").append(pageNum).append(", Offset ").append(offset).append(")\n");
            out.append("Mapped Frame:     Frame ").append(frame).append("\n");
            out.append("Formula:          (").append(frame).append(" * ").append(pageSize).append(") + ").append(offset).append("\n");
            out.append("Physical Address: ").append(physicalAddr);
        }

        OSUtils.showOutput("Paging Simulation", out.toString());
    }
}
