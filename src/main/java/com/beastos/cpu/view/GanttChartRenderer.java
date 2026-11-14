package com.beastos.cpu.view;

import com.beastos.cpu.model.GanttEntry;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

/**
 * Canvas-based Gantt chart renderer for CPU Scheduling visualization.
 * Draws process execution blocks in horizontal swim-lanes with a time axis.
 */
public final class GanttChartRenderer {

    private static final Color[] PROCESS_COLORS = {
            Color.web("#22c55e"), Color.web("#f59e0b"),
            Color.web("#2dd4bf"), Color.web("#14716a"),
            Color.web("#3b82f6"), Color.web("#a855f7"),
            Color.web("#ef4444"), Color.web("#ec4899")
    };
    private static final Color IDLE_COLOR = Color.web("#334155");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#94a3b8");
    private static final Color TEXT_MUTED = Color.web("#64748b");
    private static final Color GRID_COLOR = Color.web("#334155");
    private static final Color ARROW_COLOR = Color.web("#94a3b8");

    private static final double LEFT_MARGIN = 60;
    private static final double RIGHT_MARGIN = 20;
    private static final double TOP_MARGIN = 30;
    private static final double BOTTOM_MARGIN = 25;
    private static final double ROW_HEIGHT = 30;
    private static final double ROW_GAP = 4;
    private static final double BLOCK_RADIUS = 6;

    private GanttChartRenderer() {}

    /**
     * Render a Gantt chart onto the given canvas.
     */
    public static void render(Canvas canvas, List<GanttEntry> ganttChart) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        if (ganttChart == null || ganttChart.isEmpty()) {
            drawEmpty(gc, w, h);
            return;
        }

        // Collect unique process IDs (preserving first-seen order), excluding IDLE
        List<String> processIds = new ArrayList<>();
        Map<String, Integer> pidToRow = new LinkedHashMap<>();
        for (GanttEntry entry : ganttChart) {
            if (!entry.isIdle() && !pidToRow.containsKey(entry.getPid())) {
                pidToRow.put(entry.getPid(), processIds.size());
                processIds.add(entry.getPid());
            }
        }

        if (processIds.isEmpty()) {
            drawEmpty(gc, w, h);
            return;
        }

        // Assign colors
        Map<String, Color> colorMap = new HashMap<>();
        for (int i = 0; i < processIds.size(); i++) {
            colorMap.put(processIds.get(i), PROCESS_COLORS[i % PROCESS_COLORS.length]);
        }

        // Determine time range
        int maxTime = 0;
        for (GanttEntry entry : ganttChart) {
            maxTime = Math.max(maxTime, entry.getEndTime());
        }
        if (maxTime == 0) maxTime = 1;

        int numRows = processIds.size();
        double chartWidth = w - LEFT_MARGIN - RIGHT_MARGIN;
        double chartHeight = numRows * (ROW_HEIGHT + ROW_GAP);

        // Adjust canvas height if needed (caller should handle this ideally)
        double neededHeight = TOP_MARGIN + chartHeight + BOTTOM_MARGIN;

        // Draw column headers
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(TEXT_MUTED);
        gc.fillText("PROCESS", 8, TOP_MARGIN - 12);
        gc.fillText("EXECUTION ORDER", LEFT_MARGIN + 10, TOP_MARGIN - 12);

        // Draw grid lines
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        int tickCount = Math.min(maxTime, 10);
        if (tickCount < 2) tickCount = 2;
        // Use nice tick intervals
        int tickInterval = computeTickInterval(maxTime);
        int numTicks = maxTime / tickInterval;

        for (int i = 0; i <= numTicks; i++) {
            int t = i * tickInterval;
            double x = LEFT_MARGIN + (chartWidth * t / maxTime);
            gc.strokeLine(x, TOP_MARGIN, x, TOP_MARGIN + chartHeight);
        }

        // Draw process rows and blocks
        for (int row = 0; row < numRows; row++) {
            String pid = processIds.get(row);
            double y = TOP_MARGIN + row * (ROW_HEIGHT + ROW_GAP);

            // Process label
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.setFill(PROCESS_COLORS[row % PROCESS_COLORS.length]);
            gc.fillText(pid, 14, y + ROW_HEIGHT / 2 + 5);

            // Draw execution blocks for this process
            Color blockColor = colorMap.get(pid);
            List<GanttEntry> segments = new ArrayList<>();
            for (GanttEntry entry : ganttChart) {
                if (pid.equals(entry.getPid())) {
                    segments.add(entry);
                }
            }

            for (int s = 0; s < segments.size(); s++) {
                GanttEntry seg = segments.get(s);
                double x1 = LEFT_MARGIN + (chartWidth * seg.getStartTime() / maxTime);
                double x2 = LEFT_MARGIN + (chartWidth * seg.getEndTime() / maxTime);
                double blockW = x2 - x1;

                // Draw rounded rectangle block
                gc.setFill(blockColor);
                gc.fillRoundRect(x1, y + 2, Math.max(blockW, 4), ROW_HEIGHT - 4, BLOCK_RADIUS, BLOCK_RADIUS);

                // Block label (time range or pid)
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                gc.setFill(TEXT_PRIMARY);
                String label = pid;
                double labelWidth = label.length() * 7;
                if (blockW > labelWidth + 6) {
                    gc.fillText(label, x1 + (blockW - labelWidth) / 2, y + ROW_HEIGHT / 2 + 3);
                }

                // Arrow between segments
                if (s < segments.size() - 1) {
                    GanttEntry next = segments.get(s + 1);
                    double arrowX1 = x2 + 2;
                    double arrowX2 = LEFT_MARGIN + (chartWidth * next.getStartTime() / maxTime) - 2;
                    if (arrowX2 - arrowX1 > 8) {
                        double arrowY = y + ROW_HEIGHT / 2;
                        gc.setStroke(ARROW_COLOR);
                        gc.setLineWidth(1.5);
                        gc.strokeLine(arrowX1, arrowY, arrowX2, arrowY);
                        // Arrowhead
                        gc.strokeLine(arrowX2 - 5, arrowY - 4, arrowX2, arrowY);
                        gc.strokeLine(arrowX2 - 5, arrowY + 4, arrowX2, arrowY);
                    }
                }
            }
        }

        // Draw time axis
        gc.setFont(Font.font("Segoe UI", 11));
        gc.setFill(TEXT_MUTED);
        double axisY = TOP_MARGIN + chartHeight + 15;
        for (int i = 0; i <= numTicks; i++) {
            int t = i * tickInterval;
            double x = LEFT_MARGIN + (chartWidth * t / maxTime);
            gc.fillText(t + "ms", x - 8, axisY);
        }
        // Always show max time if not already a tick
        if (maxTime % tickInterval != 0) {
            double x = LEFT_MARGIN + chartWidth;
            gc.fillText(maxTime + "ms", x - 12, axisY);
        }
    }

    /**
     * Draw an empty-state Gantt chart placeholder.
     */
    private static void drawEmpty(GraphicsContext gc, double w, double h) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        int cols = 7;
        for (int i = 0; i <= cols; i++) {
            double x = LEFT_MARGIN + (w - LEFT_MARGIN - RIGHT_MARGIN) * i / cols;
            gc.strokeLine(x, TOP_MARGIN, x, h - BOTTOM_MARGIN);
        }

        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", 11));
        gc.fillText("PROCESS", 8, TOP_MARGIN - 8);
        gc.fillText("EXECUTION ORDER", LEFT_MARGIN + 10, TOP_MARGIN - 8);

        gc.setFont(Font.font("Segoe UI", 14));
        gc.fillText("No simulation data yet", w / 2 - 80, h / 2);

        gc.setFont(Font.font("Segoe UI", 11));
        for (int i = 0; i <= cols; i++) {
            double x = LEFT_MARGIN + (w - LEFT_MARGIN - RIGHT_MARGIN) * i / cols;
            gc.fillText(i * 4 + "ms", x - 8, h - 6);
        }
    }

    /**
     * Compute a nice tick interval for the time axis.
     */
    private static int computeTickInterval(int maxTime) {
        if (maxTime <= 10) return 1;
        if (maxTime <= 20) return 2;
        if (maxTime <= 50) return 5;
        if (maxTime <= 100) return 10;
        if (maxTime <= 200) return 20;
        if (maxTime <= 500) return 50;
        return (int) Math.pow(10, (int) Math.log10(maxTime));
    }
}
