package com.beastos.dashboard;

import com.beastos.common.FormatUtils;
import com.beastos.common.SimulationSession;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * BEAST OS Dashboard / Overview.
 * Reproduces the supplied design reference: stat cards, CPU timeline,
 * system resources donut, and simulation model grid.
 */
public class DashboardView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();

    // Theme colors
    private static final Color ACCENT = Color.web("#2dd4bf");
    private static final Color AMBER  = Color.web("#f59e0b");
    private static final Color BLUE   = Color.web("#3b82f6");
    private static final Color GREEN  = Color.web("#22c55e");
    private static final Color CARD_BG = Color.web("#1c2333");
    private static final Color TEXT_PRIMARY = Color.web("#f8fafc");
    private static final Color TEXT_SECONDARY = Color.web("#94a3b8");
    private static final Color TEXT_MUTED = Color.web("#64748b");

    // Gantt colors per process
    private static final Color[] GANTT_COLORS = {
            Color.web("#22c55e"), Color.web("#f59e0b"),
            Color.web("#2dd4bf"), Color.web("#14716a"),
            Color.web("#3b82f6"), Color.web("#a855f7"),
            Color.web("#ef4444"), Color.web("#ec4899")
    };

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
        newSimBtn.setOnAction(e -> onNewSimulation());

        // Title row with button right-aligned
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(title, spacer, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(2, tag, titleRow, subtitle);
        return header;
    }

    // ----------------------------------------------------------------
    //  B. Stat Cards
    // ----------------------------------------------------------------
    private HBox createStatCards() {
        VBox card1 = createStatCard("ACTIVE PROCESSES", "⚙",
                session.activeProcessesProperty(), false, "", GREEN);
        VBox card2 = createStatCard("CPU UTILIZATION", "\uD83D\uDCCA",
                session.cpuUtilizationProperty(), true, "Within normal range", GREEN);
        VBox card3 = createStatCard("MEMORY IN USE", "✓",
                session.memoryInUseProperty(), true, "", ACCENT);
        VBox card4 = createStatCard("SIMULATION COMPLETED", "✓",
                session.simulationsCompletedProperty(), false, "Across 8 modules", GREEN);

        HBox row = new HBox(14, card1, card2, card3, card4);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        return row;
    }

    private VBox createStatCard(String labelText, String icon, Object property,
                                 boolean isPercent, String descText, Color accentColor) {
        Label cardLabel = new Label(labelText);
        cardLabel.getStyleClass().add("stat-card-label");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-card-icon");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(cardLabel, spacer, iconLabel);
        topRow.setAlignment(Pos.CENTER);

        Label valueLabel = new Label("0");
        valueLabel.getStyleClass().add("stat-card-value");

        // Bind to property
        if (property instanceof javafx.beans.property.IntegerProperty ip) {
            valueLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> FormatUtils.zeroPad(ip.get(), 2), ip));
        } else if (property instanceof javafx.beans.property.DoubleProperty dp) {
            if (isPercent) {
                valueLabel.textProperty().bind(Bindings.createStringBinding(
                        () -> FormatUtils.formatPercent(dp.get()), dp));
            } else {
                valueLabel.textProperty().bind(Bindings.createStringBinding(
                        () -> FormatUtils.formatDecimal1(dp.get()), dp));
            }
        }

        Label descLabel = new Label(descText.isEmpty() ? " " : descText);
        descLabel.getStyleClass().add("stat-card-description");
        descLabel.setStyle("-fx-text-fill: " + toWeb(accentColor) + ";");

        // Memory card: show used/total
        if (labelText.equals("MEMORY IN USE")) {
            descLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                double used = session.getMemoryInUse();
                double total = session.getTotalMemory();
                return FormatUtils.formatDecimal1(used) + " out of " +
                       FormatUtils.formatDecimal1(total) + "GB";
            }, session.memoryInUseProperty(), session.totalMemoryProperty()));
        }

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
        resourcePanel.setMinWidth(200);

        HBox area = new HBox(14, ganttPanel, resourcePanel);
        area.setAlignment(Pos.TOP_LEFT);
        return area;
    }

    /** CPU Execution Timeline panel with empty-state Gantt. */
    private VBox createGanttPanel() {
        Label title = new Label("CPU execution timeline");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Run a CPU Scheduling simulation to view the Gantt chart");
        subtitle.getStyleClass().add("panel-subtitle");

        // Canvas for Gantt chart
        Canvas ganttCanvas = new Canvas(580, 200);
        drawEmptyGantt(ganttCanvas);

        // Redraw on resize
        ganttCanvas.widthProperty().addListener((obs, o, n) -> drawEmptyGantt(ganttCanvas));

        VBox panel = new VBox(8, title, subtitle, ganttCanvas);
        panel.getStyleClass().add("panel");
        panel.setMinHeight(280);

        // Make canvas resize with panel
        ganttCanvas.widthProperty().bind(panel.widthProperty().subtract(40));

        return panel;
    }

    private void drawEmptyGantt(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);

        // Draw grid lines
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(0.5);
        int cols = 7;
        for (int i = 0; i <= cols; i++) {
            double x = 60 + (w - 80) * i / cols;
            gc.strokeLine(x, 20, x, h - 30);
        }

        // Process labels
        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", 12));
        String[] labels = {"PROCESS", "EXECUTION ORDER"};
        gc.fillText(labels[0], 10, 15);
        gc.fillText(labels[1], 100, 15);

        // Empty state message
        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", 14));
        gc.fillText("No simulation data yet", w / 2 - 70, h / 2);

        // Time axis
        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", 11));
        for (int i = 0; i <= cols; i++) {
            double x = 60 + (w - 80) * i / cols;
            gc.fillText(i * 4 + "ms", x - 8, h - 10);
        }
    }

    /** System Resources donut chart panel. */
    private VBox createResourcePanel() {
        Label title = new Label("System Resources");
        title.getStyleClass().add("panel-title");

        Label subtitle = new Label("Live Utilization Overview");
        subtitle.getStyleClass().add("panel-subtitle");

        Canvas donutCanvas = new Canvas(240, 180);
        drawDonutChart(donutCanvas);

        // Bind to session for live updates
        session.cpuUtilizationProperty().addListener((obs, o, n) -> drawDonutChart(donutCanvas));
        session.memoryInUseProperty().addListener((obs, o, n) -> drawDonutChart(donutCanvas));
        session.resourceUtilizationProperty().addListener((obs, o, n) -> drawDonutChart(donutCanvas));

        // Legend
        VBox legend = new VBox(6,
                createLegendItem("CPU", ACCENT, session.cpuUtilizationProperty()),
                createLegendItem("Memory", AMBER, session.memoryInUseProperty()),
                createLegendItem("Resources", BLUE, session.resourceUtilizationProperty())
        );
        legend.setPadding(new Insets(0, 0, 0, 10));

        HBox chartArea = new HBox(8, donutCanvas, legend);
        chartArea.setAlignment(Pos.CENTER);

        VBox panel = new VBox(8, title, subtitle, chartArea);
        panel.getStyleClass().add("panel");
        panel.setMinWidth(340);
        panel.setPrefWidth(380);
        panel.setMaxWidth(400);
        panel.setMinHeight(280);
        return panel;
    }

    private void drawDonutChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double cx = w / 2;
        double cy = h / 2;
        double radius = Math.min(w, h) / 2 - 10;
        double innerRadius = radius * 0.6;

        double cpu = session.getCpuUtilization();
        double mem = session.getMemoryPercent();
        double res = session.getResourceUtilization();

        // If all zero, draw empty ring
        if (cpu + mem + res == 0) {
            gc.setStroke(Color.web("#334155"));
            gc.setLineWidth(radius - innerRadius);
            gc.strokeArc(cx - radius + (radius - innerRadius) / 2,
                    cy - radius + (radius - innerRadius) / 2,
                    radius * 2 - (radius - innerRadius),
                    radius * 2 - (radius - innerRadius),
                    0, 360, ArcType.OPEN);

            // Center text
            gc.setFill(TEXT_PRIMARY);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            gc.fillText("0%", cx - 14, cy + 4);
            gc.setFont(Font.font("Segoe UI", 10));
            gc.setFill(TEXT_SECONDARY);
            gc.fillText("CPU LOAD", cx - 26, cy + 18);
            return;
        }

        double total = cpu + mem + res;
        double[] values = {cpu, mem, res};
        Color[] colors = {ACCENT, AMBER, BLUE};
        double startAngle = 90;

        double thickness = radius - innerRadius;
        for (int i = 0; i < values.length; i++) {
            double sweep = (values[i] / total) * 360;
            gc.setStroke(colors[i]);
            gc.setLineWidth(thickness);
            gc.strokeArc(cx - innerRadius - thickness / 2,
                    cy - innerRadius - thickness / 2,
                    (innerRadius + thickness / 2) * 2,
                    (innerRadius + thickness / 2) * 2,
                    startAngle, sweep, ArcType.OPEN);
            startAngle += sweep;
        }

        // Center text
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        String centerText = FormatUtils.formatPercent(cpu);
        gc.fillText(centerText, cx - 28, cy + 4);
        gc.setFont(Font.font("Segoe UI", 10));
        gc.setFill(TEXT_SECONDARY);
        gc.fillText("CPU LOAD", cx - 26, cy + 18);
    }

    private HBox createLegendItem(String name, Color color, javafx.beans.property.DoubleProperty prop) {
        Region dot = new Region();
        dot.setMinSize(10, 10);
        dot.setMaxSize(10, 10);
        dot.setStyle("-fx-background-color: " + toWeb(color) + "; -fx-background-radius: 3;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label valueLabel = new Label("0.0%");
        valueLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-font-weight: bold;");
        valueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> FormatUtils.formatPercent(prop.get()), prop));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox item = new HBox(6, dot, nameLabel, spacer, valueLabel);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setMinWidth(120);
        return item;
    }

    // ----------------------------------------------------------------
    //  D. Simulation Models Grid
    // ----------------------------------------------------------------
    private VBox createModuleGrid() {
        Label title = new Label("Simulation Models");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 22px;");

        Label subtitle = new Label("Choose a workspace module");
        subtitle.getStyleClass().add("panel-subtitle");

        // Module card data: {name, description, icon}
        String[][] modules = {
                {"CPU Scheduling", "Scheduling processes & Gantt Charts", "\uD83D\uDDA5"},
                {"Banker's Algorithm", "Safety checks & resource matrices", "\uD83D\uDC64"},
                {"Paging", "Page tables & address mapping", "\uD83D\uDCC4"},
                {"Page Replacement", "FIFO · LRU · Optimal", "\uD83D\uDCC3"},
                {"Segmentation", "Segment tables & translation", "\uD83D\uDDC2"},
                {"Fragmentation", "Memory allocation strategies", "\uD83E\uDDE9"},
                {"Disk Scheduling", "Seek order & head movement", "\uD83D\uDCBF"},
                {"Multiprogramming", "Process & resource sharing", "⚙"},
        };

        // Row 1: first 4 modules
        HBox row1 = new HBox(14);
        for (int i = 0; i < 4; i++) {
            VBox card = createModuleCard(modules[i][0], modules[i][1], modules[i][2]);
            HBox.setHgrow(card, Priority.ALWAYS);
            row1.getChildren().add(card);
        }

        // Row 2: last 4 modules
        HBox row2 = new HBox(14);
        for (int i = 4; i < 8; i++) {
            VBox card = createModuleCard(modules[i][0], modules[i][1], modules[i][2]);
            HBox.setHgrow(card, Priority.ALWAYS);
            row2.getChildren().add(card);
        }

        VBox grid = new VBox(14, row1, row2);
        VBox container = new VBox(8, title, subtitle, grid);
        return container;
    }

    private VBox createModuleCard(String name, String description, String icon) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("module-card-icon");

        Label titleLabel = new Label(name);
        titleLabel.getStyleClass().add("module-card-title");

        HBox topRow = new HBox(10, iconLabel, titleLabel);
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
        card.setMinHeight(90);

        card.setOnMouseClicked(e -> {
            // Navigate to this module's parent -> use scene to find nav service
            // We access the nav service via the stage's properties
            navigateToModule(name);
        });

        return card;
    }

    private void navigateToModule(String moduleName) {
        // Walk up to find the stage, get the NavigationService from the scene
        // This is handled by the card's onMouseClicked finding the nav service
        // through the scene graph. For simplicity, we use a global accessor.
        if (getScene() != null && getScene().getWindow() != null) {
            // The NavigationService is accessed via the scene's properties
            Object navObj = getScene().getProperties().get("navService");
            if (navObj instanceof com.beastos.app.NavigationService nav) {
                nav.navigateTo(moduleName);
            }
        }
    }

    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------
    private void onNewSimulation() {
        // For Phase 1: just navigate to CPU Scheduling as the first module
        navigateToModule("CPU Scheduling");
    }

    private static String toWeb(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
