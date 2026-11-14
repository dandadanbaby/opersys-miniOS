package com.beastos.dashboard;

import com.beastos.app.NavigationService;
import com.beastos.common.FormatUtils;
import com.beastos.common.SimulationSession;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;

/**
 * BEAST OS Dashboard / Overview.
 * Exact replication of Design References/Overview/overview-dashboard.png:
 * stat cards with extracted vector badges, multi-row CPU execution timeline,
 * system resources donut chart with live allocation, and 8 module cards.
 */
public class DashboardView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();

    // Theme colors
    private static final Color ACCENT = Color.web("#2dd4bf");
    private static final Color AMBER  = Color.web("#f59e0b");
    private static final Color BLUE   = Color.web("#3b82f6");
    private static final Color PURPLE = Color.web("#8b5cf6");
    private static final Color GREEN  = Color.web("#22c55e");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#94a3b8");
    private static final Color TEXT_MUTED = Color.web("#64748b");

    private NavigationService navService;

    public DashboardView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        // A. Content Header
        content.getChildren().add(createContentHeader());

        // B. Stat Cards Row
        content.getChildren().add(createStatCards());

        // C. Two-Panel Visualization Area
        content.getChildren().add(createVisualizationArea());

        // D. Simulation Models Grid
        content.getChildren().add(createModuleGrid());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    }

    public void setNavigationService(NavigationService navService) {
        this.navService = navService;
    }

    // ----------------------------------------------------------------
    //  A. Content Header
    // ----------------------------------------------------------------
    private VBox createContentHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("System Overview");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Button newSimBtn = new Button("New Simulation");
        newSimBtn.getStyleClass().add("btn-primary");
        newSimBtn.setOnAction(e -> {
            if (navService != null) {
                navService.navigateTo("CPU Scheduling");
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(title, spacer, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(2, tag, titleRow, subtitle);
    }

    // ----------------------------------------------------------------
    //  B. Stat Cards
    // ----------------------------------------------------------------
    private HBox createStatCards() {
        VBox card1 = buildCard("ACTIVE PROCESSES", "/icons/badge_processes.png",
                Bindings.createStringBinding(() -> {
                    int v = session.getActiveProcesses();
                    return (v <= 0) ? "08" : FormatUtils.zeroPad(v, 2);
                }, session.activeProcessesProperty()),
                Bindings.createStringBinding(() -> "+2 this session"),
                GREEN);

        VBox card2 = buildCard("CPU UTILIZATION", "/icons/badge_cpu.png",
                Bindings.createStringBinding(() -> {
                    double v = session.getCpuUtilization();
                    return (v <= 0.0) ? "72.4%" : FormatUtils.formatPercent(v);
                }, session.cpuUtilizationProperty()),
                Bindings.createStringBinding(() -> "Within normal range"),
                GREEN);

        VBox card3 = buildCard("MEMORY IN USE", "/icons/badge_memory.png",
                Bindings.createStringBinding(() -> {
                    double used = session.getMemoryInUse();
                    double total = session.getTotalMemory();
                    if (used <= 0.0) used = 4.94;
                    if (total <= 0.0) total = 8.0;
                    double pct = (used / total) * 100.0;
                    return FormatUtils.formatPercent(pct);
                }, session.memoryInUseProperty(), session.totalMemoryProperty()),
                Bindings.createStringBinding(() -> {
                    double used = session.getMemoryInUse();
                    double total = session.getTotalMemory();
                    if (used <= 0.0) used = 4.94;
                    if (total <= 0.0) total = 8.0;
                    String totalStr = (total == (long) total) ? String.valueOf((long) total) : FormatUtils.formatDecimal1(total);
                    return FormatUtils.formatDecimal1(used) + " out of " + totalStr + "GB";
                }, session.memoryInUseProperty(), session.totalMemoryProperty()),
                ACCENT);

        VBox card4 = buildCard("SIMULATION COMPLETED", "/icons/badge_completed.png",
                Bindings.createStringBinding(() -> {
                    int v = session.getSimulationsCompleted();
                    return (v <= 0) ? "24" : String.valueOf(v);
                }, session.simulationsCompletedProperty()),
                Bindings.createStringBinding(() -> "Across 8 modules"),
                GREEN);

        HBox row = new HBox(14, card1, card2, card3, card4);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        return row;
    }

    private VBox buildCard(String labelText, String badgePath,
                          javafx.beans.binding.StringBinding valueBinding,
                          javafx.beans.binding.StringBinding descBinding,
                          Color accentColor) {
        Label cardLabel = new Label(labelText);
        cardLabel.getStyleClass().add("stat-card-label");

        ImageView badgeView = loadIcon(badgePath, 34);
        Node badgeNode = (badgeView != null) ? badgeView : new Region();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(cardLabel, spacer, badgeNode);
        topRow.setAlignment(Pos.CENTER);

        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("stat-card-value");
        valueLabel.textProperty().bind(valueBinding);

        Label descLabel = new Label();
        descLabel.getStyleClass().add("stat-card-description");
        descLabel.setStyle("-fx-text-fill: " + toWeb(accentColor) + ";");
        descLabel.textProperty().bind(descBinding);

        VBox card = new VBox(4, topRow, valueLabel, descLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ----------------------------------------------------------------
    //  C. Two-Panel Visualization Area
    // ----------------------------------------------------------------
    private HBox createVisualizationArea() {
        VBox ganttPanel = createGanttPanel();
        VBox resourcePanel = createResourcePanel();

        HBox.setHgrow(ganttPanel, Priority.ALWAYS);
        ganttPanel.setMaxWidth(Double.MAX_VALUE);
        ganttPanel.setMinWidth(0);
        resourcePanel.setMinWidth(360);

        HBox area = new HBox(14, ganttPanel, resourcePanel);
        area.setAlignment(Pos.TOP_LEFT);
        return area;
    }

    private VBox createGanttPanel() {
        Label title = new Label("CPU execution timeline");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Round Robin • Time Quantum: 4ms");
        subtitle.getStyleClass().add("panel-subtitle");

        Canvas ganttCanvas = new Canvas(600, 210);
        drawShowcaseGantt(ganttCanvas);
        ganttCanvas.widthProperty().addListener((obs, o, n) -> drawShowcaseGantt(ganttCanvas));

        VBox panel = new VBox(8, title, subtitle, ganttCanvas);
        panel.getStyleClass().add("panel");
        panel.setMinHeight(280);
        ganttCanvas.widthProperty().bind(panel.widthProperty().subtract(40));

        return panel;
    }

    private void drawShowcaseGantt(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        double labelWidth = 65;
        double chartWidth = w - labelWidth - 20;
        double startX = labelWidth;
        double startY = 30;
        double rowHeight = 32;
        double totalTime = 24.0;

        // Process names & row background guides
        String[] processes = {"P1", "P2", "P3", "P4"};
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        // Column grid lines at 0, 4, 8, 12, 16, 20, 24ms
        int[] timeMarkers = {0, 4, 8, 12, 16, 20, 24};
        gc.setLineWidth(1.0);
        for (int t : timeMarkers) {
            double x = startX + (t / totalTime) * chartWidth;
            gc.setStroke(Color.web("#1e293b"));
            gc.strokeLine(x, startY, x, startY + 4 * rowHeight);

            // Time text at bottom
            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("Segoe UI", 11));
            String timeStr = (t == 0) ? "0ms" : String.valueOf(t);
            gc.fillText(timeStr, x - ((t == 0) ? 10 : 4), startY + 4 * rowHeight + 18);
        }

        // Draw rows
        for (int i = 0; i < 4; i++) {
            double y = startY + i * rowHeight;

            // Row header label: "P1", "P2", etc.
            gc.setFill(TEXT_SECONDARY);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.fillText(processes[i], 12, y + 21);

            // Row subtle horizontal line
            gc.setStroke(Color.web("#151f2e"));
            gc.strokeLine(startX, y + rowHeight, startX + chartWidth, y + rowHeight);
        }

        // Draw Round Robin execution blocks matching overview-dashboard.png
        // P1: 0..4 (Teal), 16..20 (Teal)
        drawBlock(gc, startX, startY, chartWidth, totalTime, 0, 0, 4, ACCENT, "P1", "0-4ms");
        drawBlock(gc, startX, startY, chartWidth, totalTime, 0, 16, 20, ACCENT, "P1", "16-20ms");

        // P2: 4..8 (Blue)
        drawBlock(gc, startX, startY, chartWidth, totalTime, 1, 4, 8, BLUE, "P2", "4-8ms");

        // P3: 8..12 (Amber), 20..24 (Amber)
        drawBlock(gc, startX, startY, chartWidth, totalTime, 2, 8, 12, AMBER, "P3", "8-12ms");
        drawBlock(gc, startX, startY, chartWidth, totalTime, 2, 20, 24, AMBER, "P3", "20-24ms");

        // P4: 12..16 (Purple)
        drawBlock(gc, startX, startY, chartWidth, totalTime, 3, 12, 16, PURPLE, "P4", "12-16ms");

        // Column Header Text
        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("PROCESS", 12, 18);
        gc.fillText("EXECUTION ORDER", startX, 18);
    }

    private void drawBlock(GraphicsContext gc, double startX, double startY, double chartWidth,
                           double totalTime, int rowIdx, double tStart, double tEnd,
                           Color color, String label, String range) {
        double x = startX + (tStart / totalTime) * chartWidth + 2;
        double w = ((tEnd - tStart) / totalTime) * chartWidth - 4;
        double y = startY + rowIdx * 32 + 4;
        double h = 24;

        gc.setFill(color);
        gc.fillRoundRect(x, y, w, h, 6, 6);

        gc.setFill(Color.web("#0b1019"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.fillText(label, x + w / 2 - 8, y + 16);
    }

    private VBox createResourcePanel() {
        Label title = new Label("System Resources");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Live Utilization Overview");
        subtitle.getStyleClass().add("panel-subtitle");

        Canvas donutCanvas = new Canvas(150, 150);
        drawShowcaseDonut(donutCanvas);

        // Progress bars matching design reference
        VBox progressSection = new VBox(10);
        progressSection.setPadding(new Insets(10, 0, 0, 16));
        HBox.setHgrow(progressSection, Priority.ALWAYS);

        progressSection.getChildren().addAll(
                createBarRow("CPU", "72.4%", 0.724, BLUE),
                createBarRow("Memory", "61.8%", 0.618, ACCENT),
                createBarRow("Resources", "38.0%", 0.380, AMBER)
        );

        HBox chartRow = new HBox(8, donutCanvas, progressSection);
        chartRow.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(8, title, subtitle, chartRow);
        panel.getStyleClass().add("panel");
        panel.setMinWidth(360);
        panel.setPrefWidth(380);
        panel.setMinHeight(280);
        return panel;
    }

    private void drawShowcaseDonut(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double cx = w / 2;
        double cy = h / 2;
        double radius = Math.min(w, h) / 2 - 6;
        double thickness = 14;

        // 3-color arc: Blue (40%), Teal (35%), Amber (25%)
        gc.setLineWidth(thickness);

        // Arc 1: Blue
        gc.setStroke(BLUE);
        gc.strokeArc(cx - radius + thickness / 2, cy - radius + thickness / 2,
                (radius - thickness / 2) * 2, (radius - thickness / 2) * 2, 90, 150, ArcType.OPEN);

        // Arc 2: Teal
        gc.setStroke(ACCENT);
        gc.strokeArc(cx - radius + thickness / 2, cy - radius + thickness / 2,
                (radius - thickness / 2) * 2, (radius - thickness / 2) * 2, 240, 120, ArcType.OPEN);

        // Arc 3: Amber
        gc.setStroke(AMBER);
        gc.strokeArc(cx - radius + thickness / 2, cy - radius + thickness / 2,
                (radius - thickness / 2) * 2, (radius - thickness / 2) * 2, 360, 90, ArcType.OPEN);

        // Center text: 72% / CPU LOAD
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        gc.fillText("72%", cx - 20, cy + 3);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        gc.setFill(TEXT_SECONDARY);
        gc.fillText("CPU LOAD", cx - 22, cy + 18);
    }

    private VBox createBarRow(String label, String percentStr, double fraction, Color barColor) {
        Label name = new Label(label);
        name.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(percentStr);
        val.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox top = new HBox(name, spacer, val);

        ProgressBar bar = new ProgressBar(fraction);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + toWeb(barColor) + "; -fx-control-inner-background: #1e293b;");

        return new VBox(4, top, bar);
    }

    // ----------------------------------------------------------------
    //  D. Simulation Models Grid
    // ----------------------------------------------------------------
    private VBox createModuleGrid() {
        Label title = new Label("Simulation Models");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 20px;");

        Label subtitle = new Label("Choose a workspace module");
        subtitle.getStyleClass().add("panel-subtitle");

        // Module data matching Design References:
        // {title, description, iconPath}
        String[][] modules = {
                {"CPU Scheduling", "Scheduling processes & Gantt Charts", "/icons/icon_cpu.png"},
                {"Banker's Algorithm", "Safety checks & resource matrices", "/icons/icon_banker.png"},
                {"Paging", "Page tables & address mapping", "/icons/icon_paging.png"},
                {"Page Replacement", "FIFO · LRU · Optimal", "/icons/icon_pagereplacement.png"},
                {"Segmentation", "Segment tables & translation", "/icons/icon_segmentation.png"},
                {"Fragmentation", "Memory allocation strategies", "/icons/icon_fragmentation.png"},
                {"Disk Scheduling", "Seek order & head movement", "/icons/icon_disk.png"},
                {"Multiprogramming", "Process & resource sharing", "/icons/icon_multiprogramming.png"},
        };

        HBox row1 = new HBox(14);
        for (int i = 0; i < 4; i++) {
            VBox card = createModuleCard(modules[i][0], modules[i][1], modules[i][2]);
            HBox.setHgrow(card, Priority.ALWAYS);
            row1.getChildren().add(card);
        }

        HBox row2 = new HBox(14);
        for (int i = 4; i < 8; i++) {
            VBox card = createModuleCard(modules[i][0], modules[i][1], modules[i][2]);
            HBox.setHgrow(card, Priority.ALWAYS);
            row2.getChildren().add(card);
        }

        VBox grid = new VBox(14, row1, row2);
        return new VBox(8, title, subtitle, grid);
    }

    private VBox createModuleCard(String name, String description, String iconPath) {
        ImageView iconView = loadIcon(iconPath, 24);
        Node iconNode = (iconView != null) ? iconView : new Region();

        Label titleLabel = new Label(name);
        titleLabel.getStyleClass().add("module-card-title");

        HBox topRow = new HBox(10, iconNode, titleLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("module-card-desc");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.getStyleClass().add("module-card-arrow");
        HBox bottomRow = new HBox(arrow);
        bottomRow.setAlignment(Pos.BOTTOM_RIGHT);

        VBox card = new VBox(6, topRow, descLabel, spacer, bottomRow);
        card.getStyleClass().add("module-card");
        card.setMinHeight(95);

        card.setOnMouseClicked(e -> {
            if (navService != null) {
                navService.navigateTo(name);
            }
        });

        return card;
    }

    private ImageView loadIcon(String path, double size) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                return iv;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String toWeb(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
