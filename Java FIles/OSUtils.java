
import java.awt.*;
import javax.swing.*;

public class OSUtils {

    // Safely reads an integer >= minValue without crashing on blank or text input
    public static Integer getInt(String prompt, int minValue) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, prompt, "Input Required", JOptionPane.QUESTION_MESSAGE);
            if (input == null) {
                return null; // User clicked Cancel

                        }input = input.trim();
            if (input.isEmpty()) {
                showError("Input cannot be blank. Please enter a valid number.");
                continue;
            }
            try {
                int val = Integer.parseInt(input);
                if (val < minValue) {
                    showError("Value must be at least " + minValue + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                showError("Invalid number '" + input + "'. Please enter an integer.");
            }
        }
    }

    // Safely reads a space-separated list of non-negative integers (exact count if expectedCount > 0, or variable length if -1)
    public static int[] getIntArray(String prompt, int expectedCount, int minValue) {
        while (true) {
            String input = JOptionPane.showInputDialog(null, prompt, "List Input", JOptionPane.QUESTION_MESSAGE);
            if (input == null) {
                return null;
            }
            input = input.trim();
            if (input.isEmpty()) {
                showError("Input cannot be blank.");
                continue;
            }
            String[] parts = input.split("\\s+");
            if (expectedCount > 0 && parts.length != expectedCount) {
                showError("Expected exactly " + expectedCount + " numbers separated by spaces, but got " + parts.length + ".");
                continue;
            }
            int[] arr = new int[parts.length];
            boolean valid = true;
            for (int i = 0; i < parts.length; i++) {
                try {
                    arr[i] = Integer.parseInt(parts[i]);
                    if (arr[i] < minValue) {
                        showError("All values must be at least " + minValue + ".");
                        valid = false;
                        break;
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid number '" + parts[i] + "'. Please enter integers separated by spaces.");
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return arr;
            }
        }
    }

    // Displays formatted tables inside a scrollable JOptionPane (handles both small & large datasets)
    public static void showOutput(String title, String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(680, 420));
        JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Input Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
