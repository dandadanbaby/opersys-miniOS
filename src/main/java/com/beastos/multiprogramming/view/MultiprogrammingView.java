package com.beastos.multiprogramming.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.multiprogramming.engine.MultiprogrammingEngine;
import com.beastos.multiprogramming.model.MultiprogrammingOutput;
import com.beastos.multiprogramming.model.ProcessStatus;
import com.beastos.multiprogramming.model.ProgramProcess;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiprogramming module view for BEAST OS.
 * Reproduces the user's design reference: Process Resource Sharing Table,
 * System Resources donut chart + resource bars, and Live Memory Partition Map.
 */
public class MultiprogrammingView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final MultiprogrammingEngine engine = new MultiprogrammingEngine();

    // Theme colors matching design reference
    private static final Color[] PROCESS_COLORS = {
            Color.web("#2dd4bf"), // P1 - Teal
            Color.web("#38bdf8"), // P2 - Sky Blue
            Color.web("#f59e0b"), // P3 - Amber
            Color.web("#0284c7"), // P4 - Deep Blue
            Color.web("#ea580c"), // P5 - Orange
            Color.web("#a855f7"), // P6 - Purple
            Color.web("#ec4899"), // P7 - Pink
            Color.web("#10b981")  // P8 - Emerald
    };
    private static final Color FREE_COLOR = Color.web("#334155");

    // Controls
    private final TextField totalRamField = new TextField("512");
    private final VBox processListContainer = new VBox(6);
    private final List<ProcessInputRow> processRows = new ArrayList<>();
    private final Label errorLabel = new Label();

    // Stat Cards
    private final Label activeProcValue = new Label("06 / 08");
    private final Label activeProcSub = new Label("+2 in queue");
    private final Label totalRamValue = new Label("512 MB");
    private final Label memAllocValue = new Label("496 MB");
    private final Label memUtilValue = new Label("96.8%");
    private final Label memUtilSub = new Label("✓ High utilization");

    // Right Column Components
    private final Canvas donutCanvas = new Canvas(180, 180);
    private final Label cpuBarLabel = new Label("72.4%");
    private final Label memBarLabel = new Label("96.8%");
    private final Label resBarLabel = new Label("38.0%");
    private final ProgressBar cpuBar = new ProgressBar(0.724);
    private final ProgressBar memBar = new ProgressBar(0.968);
    private final ProgressBar resBar = new ProgressBar(0.380);

    // Live Memory Partition Map
    private final Canvas partitionMapCanvas = new Canvas(480, 80);

    // Active state
    private MultiprogrammingOutput currentOutput;

    public MultiprogrammingView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));
        content.setMinWidth(0);

        content.getChildren().add(createHeader());
        content.getChildren().add(createStatCards());
        content.getChildren().add(createMainWorkspace());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Load design reference initial default data:
        // 512 MB total RAM, 8 processes: [64, 128, 96, 128, 48, 32, 64, 128]
        populateDefaultData();
        runSimulation();
    }

    // ================================================================
    //  Header
    // ================================================================
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("Multiprogramming");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Label desc = new Label("Monitor process loading, memory allocation, system resource limits, and job queuing.");
        desc.getStyleClass().add("content-description");

        Button newSimBtn = new Button("New Simulation");
        newSimBtn.getStyleClass().add("btn-primary");
        newSimBtn.setOnAction(e -> resetAll());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(title, spacer, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(2, tag, titleRow, subtitle, desc);
    }

    // ================================================================
    //  Stat Cards
    // ================================================================
    private HBox createStatCards() {
        VBox c1 = buildStatCard("ACTIVE PROCESSES", activeProcValue, activeProcSub, "#38bdf8");
        activeProcSub.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px;");

        VBox c2 = buildStatCard("TOTAL SYSTEM RAM", totalRamValue, new Label("Static pool size"), "#3b82f6");

        VBox c3 = buildStatCard("MEMORY ALLOCATED", memAllocValue, new Label("Active allocations"), "#f59e0b");

        VBox c4 = buildStatCard("MEMORY UTILIZATION", memUtilValue, memUtilSub, "#22c55e");
        memUtilSub.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px;");

        HBox row = new HBox(14, c1, c2, c3, c4);
        row.setAlignment(Pos.TOP_LEFT);
        for (VBox c : new VBox[]{c1, c2, c3, c4}) {
            HBox.setHgrow(c, Priority.ALWAYS);
            c.setMinWidth(0);
        }
        return row;
    }

    private VBox buildStatCard(String label, Label value, Label sub, String accentColor) {
        Label l = new Label(label);
        l.getStyleClass().add("stat-card-label");

        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4; -fx-min-width: 8; -fx-min-height: 8; -fx-max-width: 8; -fx-max-height: 8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox top = new HBox(l, sp, dot);
        top.setAlignment(Pos.CENTER);

        value.getStyleClass().add("stat-card-value");
        sub.getStyleClass().add("stat-card-description");
        if (sub.getStyle() == null || sub.getStyle().isBlank()) {
            sub.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 11px;");
        }

        VBox card = new VBox(4, top, value, sub);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ================================================================
    //  Main Workspace: Left Table | Right Visualization Panels
    // ================================================================
    private HBox createMainWorkspace() {
        VBox leftPanel = createProcessTablePanel();
        VBox rightPanel = createRightPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.SOMETIMES);
        leftPanel.setMinWidth(0);
        rightPanel.setMinWidth(380);
        rightPanel.setPrefWidth(440);

        HBox area = new HBox(14, leftPanel, rightPanel);
        area.setAlignment(Pos.TOP_LEFT);
        return area;
    }

    // ================================================================
    //  Left: Process Resource Sharing Table Panel
    // ================================================================
    private VBox createProcessTablePanel() {
        Label title = new Label("Process Resource Sharing Table");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");

        // Controls bar: Total RAM config + Add Process + Allocate + Terminate action
        Label ramLabel = new Label("Total RAM (MB):");
        ramLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        totalRamField.setPrefWidth(80);

        Button addProcessBtn = new Button("+ Add Process");
        addProcessBtn.getStyleClass().add("btn-outline");
        addProcessBtn.setOnAction(e -> addProcessRow("", 64));

        Button allocateBtn = new Button("Allocate RAM");
        allocateBtn.getStyleClass().add("btn-accent");
        allocateBtn.setOnAction(e -> runSimulation());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox controlsBar = new HBox(8, ramLabel, totalRamField, sp, addProcessBtn, allocateBtn);
        controlsBar.setAlignment(Pos.CENTER_LEFT);

        // Table Header
        HBox tableHeader = createTableHeader();

        // Process list scroll pane
        ScrollPane processScroll = new ScrollPane(processListContainer);
        processScroll.setFitToWidth(true);
        processScroll.setPrefHeight(340);
        processScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        VBox panel = new VBox(10, title, controlsBar, tableHeader, processScroll, errorLabel);
        panel.getStyleClass().add("panel");
        panel.setMinWidth(0);
        return panel;
    }

    private HBox createTableHeader() {
        Label pCol = new Label("Process");
        pCol.setPrefWidth(80);
        pCol.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label memCol = new Label("Memory Needed");
        memCol.setPrefWidth(120);
        memCol.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label statusCol = new Label("Status");
        statusCol.setPrefWidth(180);
        statusCol.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label remCol = new Label("Remaining RAM");
        remCol.setPrefWidth(120);
        remCol.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label actCol = new Label("Action");
        actCol.setPrefWidth(80);
        actCol.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        HBox h = new HBox(8, pCol, memCol, statusCol, remCol, actCol);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-padding: 4 8; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");
        return h;
    }

    // ================================================================
    //  Right: System Resources + Live Memory Partition Map
    // ================================================================
    private VBox createRightPanel() {
        VBox sysResPanel = createSystemResourcesPanel();
        VBox partitionPanel = createPartitionMapPanel();

        VBox right = new VBox(14, sysResPanel, partitionPanel);
        right.setMinWidth(0);
        return right;
    }

    private VBox createSystemResourcesPanel() {
        Label title = new Label("System Resources");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");

        // Donut on left, bars on right
        HBox donutBox = new HBox(donutCanvas);
        donutBox.setAlignment(Pos.CENTER);
        donutBox.setMinWidth(180);

        // Progress bars
        VBox barsBox = new VBox(12);
        barsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(barsBox, Priority.ALWAYS);

        barsBox.getChildren().addAll(
                createResourceBarRow("CPU", "#2dd4bf", cpuBar, cpuBarLabel),
                createResourceBarRow("Memory", "#f59e0b", memBar, memBarLabel),
                createResourceBarRow("Resources", "#38bdf8", resBar, resBarLabel)
        );

        HBox contentBox = new HBox(14, donutBox, barsBox);
        contentBox.setAlignment(Pos.CENTER);

        VBox panel = new VBox(10, title, contentBox);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private VBox createResourceBarRow(String name, String colorHex, ProgressBar bar, Label valueLabel) {
        Label dot = new Label("● ");
        dot.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 12px;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-min-width: 65;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        valueLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox textRow = new HBox(dot, nameLabel, sp, valueLabel);
        textRow.setAlignment(Pos.CENTER_LEFT);

        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: " + colorHex + ";");

        return new VBox(4, textRow, bar);
    }

    private VBox createPartitionMapPanel() {
        Label title = new Label("Live Memory Partition Map");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 16px;");

        Label sub = new Label("RAM distribution for active processes and free space capacity.");
        sub.getStyleClass().add("panel-subtitle");

        partitionMapCanvas.widthProperty().addListener((obs, o, n) -> {
            if (currentOutput != null) {
                drawPartitionMap(currentOutput);
            }
        });

        VBox panel = new VBox(6, title, sub, partitionMapCanvas);
        panel.getStyleClass().add("panel");

        // Responsive binding
        partitionMapCanvas.widthProperty().bind(panel.widthProperty().subtract(40));

        return panel;
    }

    // ================================================================
    //  Populate Default Design Reference Data
    // ================================================================
    private void populateDefaultData() {
        totalRamField.setText("512");
        processRows.clear();
        processListContainer.getChildren().clear();

        // 8 processes from screenshot:
        int[] defaultSizes = {64, 128, 96, 128, 48, 32, 64, 128};
        for (int i = 0; i < defaultSizes.length; i++) {
            addProcessRow("P" + (i + 1), defaultSizes[i]);
        }
    }

    private void addProcessRow(String pid, int memNeeded) {
        String procId = pid.isEmpty() ? "P" + (processRows.size() + 1) : pid;
        ProcessInputRow row = new ProcessInputRow(procId, memNeeded);
        processRows.add(row);
        processListContainer.getChildren().add(row.getNode());
    }

    // ================================================================
    //  Simulation Execution
    // ================================================================
    private void runSimulation() {
        errorLabel.setText("");

        // Parse total RAM
        String ramStr = totalRamField.getText().trim();
        if (ramStr.isEmpty()) {
            errorLabel.setText("Total System RAM is required.");
            return;
        }
        int totalRam;
        try {
            totalRam = Integer.parseInt(ramStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Total System RAM must be an integer.");
            return;
        }

        // Parse process rows
        List<ProgramProcess> requests = new ArrayList<>();
        List<Integer> sizes = new ArrayList<>();
        for (ProcessInputRow row : processRows) {
            String sizeText = row.getMemoryText().trim();
            if (sizeText.isEmpty()) {
                errorLabel.setText(row.getPid() + ": memory size required.");
                return;
            }
            try {
                int s = Integer.parseInt(sizeText);
                sizes.add(s);
                requests.add(new ProgramProcess(row.getPid(), s));
            } catch (NumberFormatException e) {
                errorLabel.setText(row.getPid() + ": invalid memory number.");
                return;
            }
        }

        // Validate
        List<String> errors = engine.validate(totalRam, sizes);
        if (!errors.isEmpty()) {
            errorLabel.setText(String.join("\n", errors));
            return;
        }

        // Run simulation
        session.setSimulationState(SimulationState.RUNNING);
        currentOutput = engine.allocate(totalRam, requests);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();
        session.setActiveProcesses(currentOutput.getDegreeOfMultiprogramming());

        List<String> activeNames = new ArrayList<>();
        for (ProgramProcess p : currentOutput.getActiveProcesses()) {
            activeNames.add(p.getPid());
        }
        session.initDefaultProcesses(activeNames);

        double ramMb = currentOutput.getAllocatedMemory();
        double totalMb = currentOutput.getTotalMemory();
        session.setMemoryInUse(Math.round(((ramMb / totalMb) * session.getTotalMemory()) * 100.0) / 100.0);

        // Update UI
        updateView(currentOutput);
    }

    private void updateView(MultiprogrammingOutput output) {
        // 1. Stat cards
        activeProcValue.setText(String.format("%02d / %02d",
                output.getDegreeOfMultiprogramming(), output.getTotalProcesses()));
        if (output.getWaitingCount() > 0) {
            activeProcSub.setText("+" + output.getWaitingCount() + " in queue");
            activeProcSub.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px;");
        } else {
            activeProcSub.setText("All admitted");
            activeProcSub.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px;");
        }

        totalRamValue.setText(output.getTotalMemory() + " MB");
        memAllocValue.setText(output.getAllocatedMemory() + " MB");
        memUtilValue.setText(String.format("%.1f%%", output.getMemoryUtilization()));

        if (output.getMemoryUtilization() >= 80.0) {
            memUtilSub.setText("✓ High utilization");
            memUtilSub.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 11px;");
        } else {
            memUtilSub.setText("Moderate utilization");
            memUtilSub.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px;");
        }

        // 2. Table row statuses
        List<ProgramProcess> res = output.getProcesses();
        for (int i = 0; i < processRows.size() && i < res.size(); i++) {
            processRows.get(i).updateState(res.get(i));
        }

        // 3. System resources donut & bars
        drawDonutChart(output.getMemoryUtilization());
        cpuBar.setProgress(output.getCpuLoad() / 100.0);
        cpuBarLabel.setText(String.format("%.1f%%", output.getCpuLoad()));

        memBar.setProgress(output.getMemoryUtilization() / 100.0);
        memBarLabel.setText(String.format("%.1f%%", output.getMemoryUtilization()));

        resBar.setProgress(output.getResourceLoad() / 100.0);
        resBarLabel.setText(String.format("%.1f%%", output.getResourceLoad()));

        // 4. Live Memory Partition Map
        drawPartitionMap(output);
    }

    // ================================================================
    //  Donut Chart Drawing
    // ================================================================
    private void drawDonutChart(double memoryPercent) {
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        double w = donutCanvas.getWidth();
        double h = donutCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double cx = w / 2;
        double cy = h / 2;
        double radius = Math.min(w, h) / 2 - 10;

        // Background track (dark slate)
        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(18);
        gc.strokeArc(cx - radius + 9, cy - radius + 9, (radius - 9) * 2, (radius - 9) * 2, 0, 360, ArcType.OPEN);

        // Memory arc (Amber)
        double sweep = (memoryPercent / 100.0) * 360;
        gc.setStroke(Color.web("#f59e0b"));
        gc.strokeArc(cx - radius + 9, cy - radius + 9, (radius - 9) * 2, (radius - 9) * 2, 90, -sweep, ArcType.OPEN);

        // Center Text
        gc.setFill(Color.web("#f8fafc"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        String centerText = String.format("%.1f%%", memoryPercent);
        gc.fillText(centerText, cx - 30, cy + 4);

        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("RAM LOAD", cx - 26, cy + 18);
    }

    // ================================================================
    //  Live Memory Partition Map Drawing
    // ================================================================
    private void drawPartitionMap(MultiprogrammingOutput output) {
        GraphicsContext gc = partitionMapCanvas.getGraphicsContext2D();
        double w = partitionMapCanvas.getWidth();
        double h = partitionMapCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        int total = output.getTotalMemory();
        if (total <= 0) return;

        double currentX = 0;
        double barHeight = h - 16;
        double y = 8;

        List<ProgramProcess> activeList = output.getActiveProcesses();

        for (int i = 0; i < activeList.size(); i++) {
            ProgramProcess p = activeList.get(i);
            double blockWidth = (p.getMemoryNeeded() / (double) total) * w;
            Color color = PROCESS_COLORS[i % PROCESS_COLORS.length];

            // Filled rect
            gc.setFill(color);
            gc.fillRoundRect(currentX, y, blockWidth, barHeight, 4, 4);

            // Text label
            gc.setFill(Color.web("#0d1117"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            if (blockWidth > 28) {
                gc.fillText(p.getPid(), currentX + blockWidth / 2 - 8, y + barHeight / 2 + 4);
            }

            currentX += blockWidth;
        }

        // Free space block
        int freeMem = output.getFreeMemory();
        if (freeMem > 0 && currentX < w) {
            double freeWidth = Math.max(0, w - currentX);
            gc.setFill(FREE_COLOR);
            gc.fillRoundRect(currentX, y, freeWidth, barHeight, 4, 4);

            gc.setFill(Color.web("#94a3b8"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            String freeLabel = "Free (" + freeMem + " MB)";
            if (freeWidth > 60) {
                gc.fillText(freeLabel, currentX + freeWidth / 2 - 34, y + barHeight / 2 + 4);
            }
        }
    }

    // ================================================================
    //  Terminate Process Action
    // ================================================================
    private void terminateProcess(String pid) {
        if (currentOutput == null) return;
        session.setSimulationState(SimulationState.RUNNING);
        currentOutput = engine.terminateAndCompact(
                Integer.parseInt(totalRamField.getText().trim()),
                currentOutput.getProcesses(),
                pid
        );
        session.setSimulationState(SimulationState.COMPLETED);

        // Remove the row from the table
        processRows.removeIf(row -> row.getPid().equalsIgnoreCase(pid));
        processListContainer.getChildren().clear();
        for (ProcessInputRow r : processRows) {
            processListContainer.getChildren().add(r.getNode());
        }

        // Live update session metrics so Overview and system state reflect termination
        session.killProcess(pid);
        double ramMb = currentOutput.getAllocatedMemory();
        double totalMb = currentOutput.getTotalMemory();
        session.setMemoryInUse(Math.round(((ramMb / totalMb) * session.getTotalMemory()) * 100.0) / 100.0);

        updateView(currentOutput);
    }

    private void resetAll() {
        populateDefaultData();
        runSimulation();
    }

    // ================================================================
    //  Inner Class: Process Input Row
    // ================================================================
    private class ProcessInputRow {
        private final String pid;
        private final TextField memField;
        private final Label statusLabel;
        private final Label remLabel;
        private final Button termBtn;
        private final HBox node;

        public ProcessInputRow(String pid, int initialMem) {
            this.pid = pid;

            Label pLabel = new Label(pid);
            pLabel.setPrefWidth(80);
            pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

            memField = new TextField(String.valueOf(initialMem));
            memField.setPrefWidth(70);

            Label unitLabel = new Label("MB");
            unitLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
            HBox memBox = new HBox(4, memField, unitLabel);
            memBox.setAlignment(Pos.CENTER_LEFT);
            memBox.setPrefWidth(120);

            statusLabel = new Label("—");
            statusLabel.setPrefWidth(180);
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            remLabel = new Label("—");
            remLabel.setPrefWidth(120);
            remLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            termBtn = new Button("✕ Term");
            termBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; " +
                    "-fx-border-radius: 4; -fx-text-fill: #ef4444; -fx-font-size: 10px; -fx-cursor: hand;");
            termBtn.setOnAction(e -> terminateProcess(pid));

            node = new HBox(8, pLabel, memBox, statusLabel, remLabel, termBtn);
            node.setAlignment(Pos.CENTER_LEFT);
            node.setStyle("-fx-padding: 6 8; -fx-background-color: #161b22; -fx-background-radius: 6;");
        }

        public void updateState(ProgramProcess p) {
            if (p.isActive()) {
                statusLabel.setText("● Active in Memory");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
                node.setStyle("-fx-padding: 6 8; -fx-background-color: #161b22; -fx-background-radius: 6;");
            } else if (p.getStatus() == ProcessStatus.WAITING) {
                statusLabel.setText("● Waiting (RAM Full)");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                node.setStyle("-fx-padding: 6 8; -fx-background-color: #1e1e24; -fx-border-color: #f59e0b; " +
                        "-fx-border-radius: 6; -fx-border-width: 1; -fx-background-radius: 6;");
            } else {
                statusLabel.setText("Terminated");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            }
            remLabel.setText(p.getRemainingRam() + " MB");
        }

        public String getPid() { return pid; }
        public String getMemoryText() { return memField.getText(); }
        public HBox getNode() { return node; }
    }
}
