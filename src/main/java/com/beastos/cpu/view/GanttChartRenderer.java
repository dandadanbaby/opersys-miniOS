package com.beastos.cpu.view;

import com.beastos.cpu.model.GanttEntry;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

/*
 * GanttChartRenderer - our custom canvas renderer for the CPU Gantt chart!
 * Instead of doing just a single boring horizontal bar, we decided to make separate
 * swim-lane rows for each process. That way, whenever a process gets preempted
 * (like in Round Robin or SRTF) and resumes later, we can see its full lifecycle across time.
 */
public final class GanttChartRenderer {

    // our color palette for processes so each one stands out clearly on dark mode
    private static final Color[] PROCESS_COLORS = {
            Color.web("#22c55e"), Color.web("#f59e0b"),
            Color.web("#2dd4bf"), Color.web("#14716a"),
            Color.web("#3b82f6"), Color.web("#a855f7"),
            Color.web("#ef4444"), Color.web("#ec4899")
    };
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_MUTED = Color.web("#64748b");
    private static final Color GRID_COLOR = Color.web("#334155");
    private static final Color ARROW_COLOR = Color.web("#94a3b8");

    // layout margins and row sizing we tweaked so text labels don't get clipped
    private static final double LEFT_MARGIN = 60;
    private static final double RIGHT_MARGIN = 20;
    private static final double TOP_MARGIN = 30;
    private static final double BOTTOM_MARGIN = 25;
    private static final double ROW_HEIGHT = 30;
    private static final double ROW_GAP = 4;
    private static final double BLOCK_RADIUS = 6;

    private GanttChartRenderer() {}

    // this is our main render method that draws the entire Gantt chart onto the canvas
    public static void render(Canvas canvas, List<GanttEntry> ganttChart) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        // if there's no simulation data yet, just draw the empty grid placeholder
        if (ganttChart == null || ganttChart.isEmpty()) {
            drawEmpty(gc, w, h);
            return;
        }

        // we collect unique process IDs in the order they first appeared, skipping IDLE
        // because CPU idle intervals don't get their own process row in our swim-lane design
        List<String> processIds = new ArrayList<>();
        Map<String, Integer> pidToRow = new LinkedHashMap<>();
        for (GanttEntry entry : ganttChart) {
            if (!entry.isIdle() && !pidToRow.containsKey(entry.getPid())) {
                pidToRow.put(entry.getPid(), processIds.size());
                processIds.add(entry.getPid());
            }
        }

        // if everything was idle for some reason, we treat it as empty
        if (processIds.isEmpty()) {
            drawEmpty(gc, w, h);
            return;
        }

        // assign each process a consistent color from our palette (cycling if > 8 processes)
        Map<String, Color> colorMap = new HashMap<>();
        for (int i = 0; i < processIds.size(); i++) {
            colorMap.put(processIds.get(i), PROCESS_COLORS[i % PROCESS_COLORS.length]);
        }

        // figure out the total execution time so we can scale pixel widths to match the timeline
        double maxTime = 0;
        for (GanttEntry entry : ganttChart) {
            maxTime = Math.max(maxTime, entry.getEndTime());
        }
        if (maxTime <= 0) maxTime = 1;

        int numRows = processIds.size();
        double chartWidth = w - LEFT_MARGIN - RIGHT_MARGIN;
        double chartHeight = numRows * (ROW_HEIGHT + ROW_GAP);

        // draw column headers at the top so it's clear what's on the left vs the right
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(TEXT_MUTED);
        gc.fillText("PROCESS", 8, TOP_MARGIN - 12);
        gc.fillText("EXECUTION ORDER", LEFT_MARGIN + 10, TOP_MARGIN - 12);

        // draw vertical grid lines across all rows to help track time progression
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        double tickInterval = computeTickInterval(maxTime);
        int numTicks = (int) Math.floor(maxTime / tickInterval);

        for (int i = 0; i <= numTicks; i++) {
            double t = i * tickInterval;
            double x = LEFT_MARGIN + (chartWidth * t / maxTime);
            gc.strokeLine(x, TOP_MARGIN, x, TOP_MARGIN + chartHeight);
        }

        // now we draw each process's row and all its execution segments
        for (int row = 0; row < numRows; row++) {
            String pid = processIds.get(row);
            double y = TOP_MARGIN + row * (ROW_HEIGHT + ROW_GAP);

            // draw the PID label on the left margin using that process's color
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.setFill(PROCESS_COLORS[row % PROCESS_COLORS.length]);
            gc.fillText(pid, 14, y + ROW_HEIGHT / 2 + 5);

            // filter out just the Gantt entries belonging to this specific process
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

                // draw the rounded block — min width 4px so tiny 1ms bursts don't disappear completely
                gc.setFill(blockColor);
                gc.fillRoundRect(x1, y + 2, Math.max(blockW, 4), ROW_HEIGHT - 4, BLOCK_RADIUS, BLOCK_RADIUS);

                // only print the PID inside the block if it's wide enough, otherwise text spills outside
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                gc.setFill(TEXT_PRIMARY);
                String label = pid;
                double labelWidth = label.length() * 7;
                if (blockW > labelWidth + 6) {
                    gc.fillText(label, x1 + (blockW - labelWidth) / 2, y + ROW_HEIGHT / 2 + 3);
                }

                // if this process was preempted and runs again later, we draw a little connector arrow
                // basically what happens here is showing that the process was paused while something else ran
                if (s < segments.size() - 1) {
                    GanttEntry next = segments.get(s + 1);
                    double arrowX1 = x2 + 2;
                    double arrowX2 = LEFT_MARGIN + (chartWidth * next.getStartTime() / maxTime) - 2;
                    if (arrowX2 - arrowX1 > 8) {
                        double arrowY = y + ROW_HEIGHT / 2;
                        gc.setStroke(ARROW_COLOR);
                        gc.setLineWidth(1.5);
                        gc.strokeLine(arrowX1, arrowY, arrowX2, arrowY);
                        // arrowhead pointing to where execution resumes
                        gc.strokeLine(arrowX2 - 5, arrowY - 4, arrowX2, arrowY);
                        gc.strokeLine(arrowX2 - 5, arrowY + 4, arrowX2, arrowY);
                    }
                }
            }
        }

        // draw the time axis labels along the bottom in milliseconds
        gc.setFont(Font.font("Segoe UI", 11));
        gc.setFill(TEXT_MUTED);
        double axisY = TOP_MARGIN + chartHeight + 15;
        for (int i = 0; i <= numTicks; i++) {
            double t = i * tickInterval;
            double x = LEFT_MARGIN + (chartWidth * t / maxTime);
            gc.fillText(formatTime(t) + "ms", x - 8, axisY);
        }
        // we always want to show the final completion timestamp even if it doesn't land on an exact tick
        if (Math.abs(maxTime - (numTicks * tickInterval)) > 1e-4) {
            double x = LEFT_MARGIN + chartWidth;
            gc.fillText(formatTime(maxTime) + "ms", x - 12, axisY);
        }
    }

    // draws a blank placeholder grid so the UI doesn't look empty and broken before the user clicks simulate
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

    // helper we came up with to pick clean round tick intervals (1, 2, 5, 10, etc.) so the time axis looks neat
    private static double computeTickInterval(double maxTime) {
        if (maxTime <= 2) return 0.2;
        if (maxTime <= 5) return 0.5;
        if (maxTime <= 10) return 1;
        if (maxTime <= 20) return 2;
        if (maxTime <= 50) return 5;
        if (maxTime <= 100) return 10;
        if (maxTime <= 200) return 20;
        if (maxTime <= 500) return 50;
        return Math.pow(10, Math.floor(Math.log10(maxTime)));
    }

    private static String formatTime(double val) {
        if (Math.abs(val - Math.round(val)) < 1e-6) {
            return String.valueOf(Math.round(val));
        } else {
            return String.format(Locale.US, "%.1f", val);
        }
    }
}
