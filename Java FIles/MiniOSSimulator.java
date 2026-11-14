
import javax.swing.JOptionPane;

public class MiniOSSimulator {

    public static void main(String[] args) {
        String[] modules = {
            "CPU Scheduling",
            "Deadlock - Banker's Algorithm",
            "Paging",
            "Page Replacement",
            "Segmentation",
            "Fragmentation",
            "Disk Scheduling",
            "Multiprogramming",
            "Exit"
        };

        while (true) {
            String choice = (String) JOptionPane.showInputDialog(
                    null,
                    "Select a module to open:",
                    "Mini OS Simulator",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    modules,
                    modules[0]
            );

            if (choice == null || choice.equals("Exit")) {
                break;
            }

            switch (choice) {
                case "CPU Scheduling":
                    CPUScheduling.run();
                    break;
                case "Deadlock - Banker's Algorithm":
                    BankersAlgorithm.run();
                    break;
                case "Paging":
                    Paging.run();
                    break;
                case "Page Replacement":
                    PageReplacement.run();
                    break;
                case "Segmentation":
                    Segmentation.run();
                    break;
                case "Fragmentation":
                    Fragmentation.run();
                    break;
                case "Disk Scheduling":
                    DiskScheduling.run();
                    break;
                case "Multiprogramming":
                    MultiProgramming.run();
                    break;
            }
        }
    }
}
