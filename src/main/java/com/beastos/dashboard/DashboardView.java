package com.beastos.dashboard;

import com.beastos.app.NavigationService;
import com.beastos.common.FormatUtils;
import com.beastos.common.SimulationSession;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.List;

/*
 * Hey guys, this is our main BEAST OS Dashboard overview!
 * We put together this screen to show a high-level picture of what's happening in the system:
 * quick stat cards at the top, a live Round Robin Gantt timeline next to a resource donut chart,
 * and cards at the bottom linking directly to our 8 simulation modules.
 */
public class DashboardView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();

    // our color palette for the dark theme so everything matches across the dashboard
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

        // section A: title banner and the "New Simulation" button
        content.getChildren().add(createContentHeader());

        // section B: top row cards showing live metrics (active procs, CPU %, memory)
        content.getChildren().add(createStatCards());

        // section C: middle split view with the Gantt chart and resource donut chart
        content.getChildren().add(createVisualizationArea());

        // section D: 2x4 card grid to navigate to each OS simulation module
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
    //  A. Header banner with quick navigation
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

        // spacer pushes the button over to the far right of the header
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(title, spacer, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(2, tag, titleRow, subtitle);
    }

    // ----------------------------------------------------------------
    //  B. Stat Cards — we bound these to SimulationSession so they update live!
    // ----------------------------------------------------------------
    private HBox createStatCards() {
        // active process count in memory — formatted with zeroPad so "05" looks clean
        VBox card1 = buildCard("ACTIVE PROCESSES", "/icons/badge_processes.png",
                Bindings.createStringBinding(() -> {
                    int v = session.getActiveProcesses();
                    return FormatUtils.zeroPad(Math.max(0, v), 2);
                }, session.activeProcessesProperty()),
                Bindings.createStringBinding(() -> {
                    int v = session.getActiveProcesses();
                    if (v == 0) return "All processes terminated";
                    if (v >= 6) return "+2 this session";
                    return v + " active in memory";
                }, session.activeProcessesProperty()),
                GREEN);

        // overall CPU load percentage
        VBox card2 = buildCard("CPU UTILIZATION", "/icons/badge_cpu.png",
                Bindings.createStringBinding(() -> {
                    double v = session.getCpuUtilization();
                    return FormatUtils.formatPercent(v);
                }, session.cpuUtilizationProperty()),
                Bindings.createStringBinding(() -> {
                    double v = session.getCpuUtilization();
                    if (v <= 10.0) return "Idle / standby";
                    if (v >= 85.0) return "Heavy compute";
                    return "Within normal range";
                }, session.cpuUtilizationProperty()),
                GREEN);

        // memory in use — we default to 8GB if total is 0 to avoid dividing by zero lol
        VBox card3 = buildCard("MEMORY IN USE", "/icons/badge_memory.png",
                Bindings.createStringBinding(() -> {
                    double used = session.getMemoryInUse();
                    double total = session.getTotalMemory();
                    if (total <= 0.0) total = 8.0;
                    double pct = (used / total) * 100.0;
                    return FormatUtils.formatPercent(pct);
                }, session.memoryInUseProperty(), session.totalMemoryProperty()),
                Bindings.createStringBinding(() -> {
                    double used = session.getMemoryInUse();
                    double total = session.getTotalMemory();
                    if (total <= 0.0) total = 8.0;
                    String totalStr = (total == (long) total) ? String.valueOf((long) total) : FormatUtils.formatDecimal1(total);
                    return FormatUtils.formatDecimal1(used) + " out of " + totalStr + "GB";
                }, session.memoryInUseProperty(), session.totalMemoryProperty()),
                ACCENT);

        // total simulation runs completed across all modules
        VBox card4 = buildCard("SIMULATION COMPLETED", "/icons/badge_completed.png",
                Bindings.createStringBinding(() -> {
                    int v = session.getSimulationsCompleted();
                    return String.valueOf(Math.max(0, v));
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

    // helper method to build each stat card so we don't have to duplicate the card layout 4 times
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
    //  C. Two-Panel Visualization Area: Gantt timeline + Resource Donut
    // ----------------------------------------------------------------
    private HBox createVisualizationArea() {
        VBox ganttPanel = createGanttPanel();
        VBox resourcePanel = createResourcePanel();

        // we let the Gantt panel expand to fill available horizontal space,
        // but keep the resource panel at least 360px wide so the donut chart doesn't cramp
        HBox.setHgrow(ganttPanel, Priority.ALWAYS);
        ganttPanel.setMaxWidth(Double.MAX_VALUE);
        ganttPanel.setMinWidth(0);
        resourcePanel.setMinWidth(360);

        HBox area = new HBox(14, ganttPanel, resourcePanel);
        area.setAlignment(Pos.TOP_LEFT);
        return area;
    }

    // this panel hosts the CPU execution timeline canvas and process chips
    private VBox createGanttPanel() {
        Label title = new Label("CPU execution timeline");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Round Robin • Time Quantum: 4ms");
        subtitle.getStyleClass().add("panel-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox processChips = new HBox(6);
        processChips.setAlignment(Pos.CENTER_RIGHT);

        HBox headerRow = new HBox(10, new VBox(2, title, subtitle), spacer, processChips);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Canvas ganttCanvas = new Canvas(600, 210);

        Runnable refreshGantt = () -> {
            updateProcessChips(processChips);
            drawLiveGantt(ganttCanvas);
        };

        refreshGantt.run();

        // listen for process list changes or window resizing and redraw on the fly
        session.getActiveProcessNames().addListener((ListChangeListener<String>) c -> refreshGantt.run());
        session.activeProcessesProperty().addListener((obs, o, n) -> refreshGantt.run());
        ganttCanvas.widthProperty().addListener((obs, o, n) -> drawLiveGantt(ganttCanvas));

        VBox panel = new VBox(8, headerRow, ganttCanvas);
        panel.getStyleClass().add("panel");
        panel.setMinHeight(280);
        // binding the canvas width to panel width minus padding keeps it responsive!
        ganttCanvas.widthProperty().bind(panel.widthProperty().subtract(40));

        return panel;
    }

    // updates the little process tag chips up in the timeline header — we added a kill button on each
    // and a spawn button so we can test dynamic scheduling without opening another tab
    private void updateProcessChips(HBox container) {
        container.getChildren().clear();

        Label label = new Label("Active Procs:");
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        container.getChildren().add(label);

        for (String pid : session.getActiveProcessNames()) {
            HBox chip = new HBox(4);
            chip.setAlignment(Pos.CENTER);
            chip.setStyle("-fx-background-color: #161b22; -fx-border-color: #334155; -fx-border-radius: 4; -fx-padding: 2 6;");

            Label pLabel = new Label(pid);
            pLabel.setStyle("-fx-text-fill: #2dd4bf; -fx-font-size: 11px; -fx-font-weight: bold;");

            Button killBtn = new Button("✕");
            killBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 10px; -fx-padding: 0 2; -fx-cursor: hand;");
            killBtn.setTooltip(new Tooltip("Terminate process " + pid));
            killBtn.setOnAction(e -> session.killProcess(pid));

            chip.getChildren().addAll(pLabel, killBtn);
            container.getChildren().add(chip);
        }

        Button spawnBtn = new Button("+ Spawn");
        spawnBtn.setStyle("-fx-background-color: #0f766e; -fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-radius: 4; -fx-padding: 2 7; -fx-cursor: hand;");
        spawnBtn.setTooltip(new Tooltip("Spawn new process live"));
        spawnBtn.setOnAction(e -> session.spawnProcess());
        container.getChildren().add(spawnBtn);
    }

    // this is where we actually draw the Gantt chart onto the Canvas
    private void drawLiveGantt(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        List<String> procs = session.getActiveProcessNames();
        // if no processes are running, show an idle placeholder message instead of an empty box
        if (procs.isEmpty()) {
            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            gc.fillText("All processes terminated • System idle (0 active processes)", w / 2 - 170, h / 2 - 5);
            gc.setFont(Font.font("Segoe UI", 12));
            gc.fillText("Click [+ Spawn] above or open CPU Scheduling / Multiprogramming to launch simulations", w / 2 - 220, h / 2 + 18);
            return;
        }

        int procCount = procs.size();
        double labelWidth = 65;
        double chartWidth = w - labelWidth - 20;
        double startX = labelWidth;
        double startY = 30;
        double rowHeight = Math.min(32.0, (h - startY - 25) / Math.max(1, procCount));
        double totalTime = Math.max(16.0, procCount * 4.0);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        // Time markers: grid lines and millisecond timestamps across the bottom
        int step = (int) Math.max(4, Math.round(totalTime / 6.0));
        gc.setLineWidth(1.0);
        for (int t = 0; t <= totalTime; t += step) {
            double x = startX + (t / totalTime) * chartWidth;
            gc.setStroke(Color.web("#1e293b"));
            gc.strokeLine(x, startY, x, startY + procCount * rowHeight);

            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("Segoe UI", 11));
            String timeStr = (t == 0) ? "0ms" : t + "ms";
            gc.fillText(timeStr, x - ((t == 0) ? 10 : 6), startY + procCount * rowHeight + 16);
        }

        Color[] colors = {ACCENT, BLUE, AMBER, PURPLE, Color.web("#38bdf8"), Color.web("#ec4899"), Color.web("#10b981"), Color.web("#ea580c")};

        // draw rows for each process showing their Round Robin time slices
        for (int i = 0; i < procCount; i++) {
            double y = startY + i * rowHeight;
            gc.setFill(TEXT_SECONDARY);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.fillText(procs.get(i), 12, y + rowHeight * 0.65);

            gc.setStroke(Color.web("#151f2e"));
            gc.strokeLine(startX, y + rowHeight, startX + chartWidth, y + rowHeight);

            // here's where we simulate Round Robin scheduling: each process gets a 4ms time slice,
            // and we illustrate subsequent rounds so the user sees CPU preemption in action!
            Color c = colors[i % colors.length];
            double sliceTime = 4.0;
            double t1 = i * 4.0;
            if (t1 < totalTime) {
                drawBlock(gc, startX, startY, chartWidth, totalTime, i, t1, Math.min(totalTime, t1 + sliceTime), rowHeight, c, procs.get(i));
            }
            double t2 = t1 + procCount * 4.0;
            if (t2 < totalTime) {
                drawBlock(gc, startX, startY, chartWidth, totalTime, i, t2, Math.min(totalTime, t2 + sliceTime), rowHeight, c, procs.get(i));
            }
        }

        // column header labels at the top of the chart
        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("PROCESS", 12, 18);
        gc.fillText("EXECUTION ORDER (Round Robin • 4ms Quantum)", startX, 18);
    }

    // helper to draw one colored execution block (time slice) on the Gantt timeline
    private void drawBlock(GraphicsContext gc, double startX, double startY, double chartWidth,
                           double totalTime, int rowIdx, double tStart, double tEnd,
                           double rowHeight, Color color, String label) {
        double x = startX + (tStart / totalTime) * chartWidth + 2;
        double w = Math.max(4, ((tEnd - tStart) / totalTime) * chartWidth - 4);
        double y = startY + rowIdx * rowHeight + 3;
        double h = Math.max(12, rowHeight - 6);

        gc.setFill(color);
        gc.fillRoundRect(x, y, w, h, 6, 6);

        gc.setFill(Color.web("#0b1019"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        if (w > 20) {
            gc.fillText(label, x + w / 2 - 8, y + h / 2 + 4);
        }
    }

    // right panel showing system resources with our custom donut chart + progress bars
    private VBox createResourcePanel() {
        Label title = new Label("System Resources");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Live Utilization Overview");
        subtitle.getStyleClass().add("panel-subtitle");

        Canvas donutCanvas = new Canvas(150, 150);

        Runnable refreshDonut = () -> drawLiveDonut(donutCanvas);
        refreshDonut.run();

        // redraw donut whenever any resource stat updates
        session.cpuUtilizationProperty().addListener((obs, o, n) -> refreshDonut.run());
        session.memoryInUseProperty().addListener((obs, o, n) -> refreshDonut.run());
        session.totalMemoryProperty().addListener((obs, o, n) -> refreshDonut.run());
        session.resourceUtilizationProperty().addListener((obs, o, n) -> refreshDonut.run());

        // reactive progress bars for CPU, Memory, and peripheral resources
        VBox progressSection = new VBox(10);
        progressSection.setPadding(new Insets(10, 0, 0, 16));
        HBox.setHgrow(progressSection, Priority.ALWAYS);

        progressSection.getChildren().addAll(
                createLiveBarRow("CPU",
                        Bindings.createStringBinding(() -> FormatUtils.formatPercent(session.getCpuUtilization()), session.cpuUtilizationProperty()),
                        Bindings.createDoubleBinding(() -> Math.min(1.0, Math.max(0.0, session.getCpuUtilization() / 100.0)), session.cpuUtilizationProperty()),
                        BLUE),
                createLiveBarRow("Memory",
                        Bindings.createStringBinding(() -> FormatUtils.formatPercent(session.getMemoryPercent()), session.memoryInUseProperty(), session.totalMemoryProperty()),
                        Bindings.createDoubleBinding(() -> Math.min(1.0, Math.max(0.0, session.getMemoryPercent() / 100.0)), session.memoryInUseProperty(), session.totalMemoryProperty()),
                        ACCENT),
                createLiveBarRow("Resources",
                        Bindings.createStringBinding(() -> FormatUtils.formatPercent(session.getResourceUtilization()), session.resourceUtilizationProperty()),
                        Bindings.createDoubleBinding(() -> Math.min(1.0, Math.max(0.0, session.getResourceUtilization() / 100.0)), session.resourceUtilizationProperty()),
                        AMBER)
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

    // draws our 3-part donut chart: CPU (blue), Memory (teal), and Resources (amber)
    private void drawLiveDonut(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double cx = w / 2;
        double cy = h / 2;
        double radius = Math.min(w, h) / 2 - 6;
        double thickness = 14;

        // calculate proportional arc angles so the 3 slices fill 360 degrees
        double cpuFrac = Math.min(1.0, Math.max(0.05, session.getCpuUtilization() / 100.0));
        double memFrac = Math.min(1.0, Math.max(0.05, session.getMemoryPercent() / 100.0));
        double resFrac = Math.min(1.0, Math.max(0.05, session.getResourceUtilization() / 100.0));
        double sum = cpuFrac + memFrac + resFrac;
        if (sum <= 0) sum = 1.0;

        double arc1 = (cpuFrac / sum) * 360.0;
        double arc2 = (memFrac / sum) * 360.0;
        double arc3 = 360.0 - arc1 - arc2;

        gc.setLineWidth(thickness);

        // Arc 1: Blue represents CPU share
        gc.setStroke(BLUE);
        gc.strokeArc(cx - radius + thickness / 2, cy - radius + thickness / 2,
                (radius - thickness / 2) * 2, (radius - thickness / 2) * 2, 90, arc1, ArcType.OPEN);

        // Arc 2: Teal represents Memory share
        gc.setStroke(ACCENT);
        gc.strokeArc(cx - radius + thickness / 2, cy - radius + thickness / 2,
                (radius - thickness / 2) * 2, (radius - thickness / 2) * 2, 90 + arc1, arc2, ArcType.OPEN);

        // Arc 3: Amber represents other System Resources
        gc.setStroke(AMBER);
        gc.strokeArc(cx - radius + thickness / 2, cy - radius + thickness / 2,
                (radius - thickness / 2) * 2, (radius - thickness / 2) * 2, 90 + arc1 + arc2, arc3, ArcType.OPEN);

        // center cutout text: big CPU percentage and label in the middle of the donut
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        String cpuPct = String.format("%.0f%%", session.getCpuUtilization());
        gc.fillText(cpuPct, cx - (cpuPct.length() > 3 ? 24 : 18), cy + 3);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        gc.setFill(TEXT_SECONDARY);
        gc.fillText("CPU LOAD", cx - 22, cy + 18);
    }

    // creates a single resource progress bar with its label, current percentage, and bar
    private VBox createLiveBarRow(String label, javafx.beans.binding.StringBinding textBinding,
                                  javafx.beans.binding.DoubleBinding progressBinding, Color barColor) {
        Label name = new Label(label);
        name.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label();
        val.textProperty().bind(textBinding);
        val.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox top = new HBox(name, spacer, val);

        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(progressBinding);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + toWeb(barColor) + "; -fx-control-inner-background: #1e293b;");

        return new VBox(4, top, bar);
    }

    // ----------------------------------------------------------------
    //  D. 8-Module Navigation Grid
    // ----------------------------------------------------------------
    private VBox createModuleGrid() {
        Label title = new Label("Simulation Models");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 20px;");

        Label subtitle = new Label("Choose a workspace module");
        subtitle.getStyleClass().add("panel-subtitle");

        // metadata for each OS module: {title, short description, icon path}
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

        // we arrange them into two rows of 4 cards each for a neat 4x2 grid
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

    // builds an individual clickable card for the module grid
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

        // clicking anywhere on the card navigates straight to that module view
        card.setOnMouseClicked(e -> {
            if (navService != null) {
                navService.navigateTo(name);
            }
        });

        return card;
    }

    // helper to safely load icons from our resources folder so missing files don't crash the app
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

    // simple helper to convert JavaFX Color into a hex code string like #2DD4BF for CSS inline styles
    private String toWeb(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
