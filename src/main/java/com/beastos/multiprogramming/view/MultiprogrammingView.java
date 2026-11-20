package com.beastos.multiprogramming.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.multiprogramming.engine.MultiprogrammingEngine;
import com.beastos.multiprogramming.model.*;

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

public class MultiprogrammingView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final MultiprogrammingEngine engine = new MultiprogrammingEngine();

    private static final Color[] JOB_COLORS = {
            Color.web("#2dd4bf"), Color.web("#38bdf8"), Color.web("#f59e0b"), Color.web("#0284c7"),
            Color.web("#ea580c"), Color.web("#a855f7"), Color.web("#ec4899"), Color.web("#10b981")
    };

    private final VBox jobListContainer = new VBox(6);
    private final VBox traceContainer = new VBox(4);
    private final List<JobInputRow> jobRows = new ArrayList<>();
    private final Label errorLabel = new Label();

    // Top 4 Stat Cards (updated to ar/bt metrics)
    private final Label jobsCountValue = new Label("05");
    private final Label jobsCountSub = new Label("Peak concurrency: 3");
    private final Label totalBurstValue = new Label("1.40 s");
    private final Label finishTimeValue = new Label("11.40");
    private final Label cpuUtilValue = new Label("100.0%");
    private final Label cpuUtilSub = new Label("✓ Optimal sharing");

    // Right Column Components
    private final Canvas donutCanvas = new Canvas(180, 180);
    private final Label cpuBarLabel = new Label("100.0%");
    private final Label shareBarLabel = new Label("33.3%");
    private final Label tatBarLabel = new Label("0.64");
    private final ProgressBar cpuBar = new ProgressBar(1.0);
    private final ProgressBar shareBar = new ProgressBar(0.333);
    private final ProgressBar tatBar = new ProgressBar(0.64);

    // Bottom Right: Job Burst Distribution Bar
    private final Canvas burstMapCanvas = new Canvas(480, 80);
    private MultiprogrammingOutput currentOutput;

    // Execution mode selection (Processor Sharing vs Round Robin)
    private MultiprogrammingEngine.ExecutionMode selectedMode = MultiprogrammingEngine.ExecutionMode.PROCESSOR_SHARING;
    private final List<Button> modeTabs = new ArrayList<>();
    private final TextField quantumField = new TextField("0.2");
    private final HBox quantumBox = new HBox(6);

    public MultiprogrammingView() {
        VBox content = new VBox(20, createHeader(), createStatCards(), createMainWorkspace(), createTracePanel());
        content.setPadding(new Insets(0));
        content.setMinWidth(0);

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        populateDefaultData();
        runSimulation();
    }

    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("Multiprogramming");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Label desc = new Label("Simulate concurrent job execution, arrival times (ar), burst times (bt), and 1/nj dynamic CPU sharing.");
        desc.getStyleClass().add("content-description");

        Button newSimBtn = new Button("New Simulation");
        newSimBtn.getStyleClass().add("btn-primary");
        newSimBtn.setOnAction(e -> { populateDefaultData(); runSimulation(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(title, spacer, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(2, tag, titleRow, subtitle, desc);
    }

    private HBox createStatCards() {
        VBox c1 = buildStatCard("ACTIVE JOBS", jobsCountValue, jobsCountSub, "#38bdf8");
        VBox c2 = buildStatCard("TOTAL CPU WORK", totalBurstValue, new Label("Sum of burst times"), "#3b82f6");
        VBox c3 = buildStatCard("COMPLETION TIME", finishTimeValue, new Label("Final termination"), "#f59e0b");
        VBox c4 = buildStatCard("CPU UTILIZATION", cpuUtilValue, cpuUtilSub, "#22c55e");

        HBox row = new HBox(14, c1, c2, c3, c4);
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
        sub.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 11px;");

        VBox card = new VBox(4, top, value, sub);
        card.getStyleClass().add("stat-card");
        return card;
    }

    private HBox createMainWorkspace() {
        VBox leftPanel = createJobTablePanel();
        VBox rightPanel = createRightPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.SOMETIMES);
        leftPanel.setMinWidth(0);
        rightPanel.setMinWidth(380);
        rightPanel.setPrefWidth(440);

        return new HBox(14, leftPanel, rightPanel);
    }

    private VBox createJobTablePanel() {
        Label title = new Label("Job Arrival & Burst Table");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");

        HBox modeSelector = createModeSelector();

        Button addJobBtn = new Button("+ Add Job");
        addJobBtn.getStyleClass().add("btn-outline");
        addJobBtn.setOnAction(e -> {
            double nextAr = jobRows.isEmpty() ? 10.0 : Math.round((jobRows.get(jobRows.size() - 1).getArVal() + 0.2) * 100.0) / 100.0;
            addJobRow("", nextAr, 0.2);
        });

        Button runBtn = new Button("Run Simulation");
        runBtn.getStyleClass().add("btn-accent");
        runBtn.setOnAction(e -> runSimulation());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox controlsBar = new HBox(8, modeSelector, sp, addJobBtn, runBtn);
        controlsBar.setAlignment(Pos.CENTER_LEFT);

        HBox tableHeader = new HBox(8,
                colHeader("Jobs", 70),
                colHeader("ar", 100),
                colHeader("bt", 100),
                colHeader("Finish", 95),
                colHeader("TAT", 90),
                colHeader("Action", 75)
        );
        tableHeader.setStyle("-fx-padding: 4 8; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        ScrollPane jobScroll = new ScrollPane(jobListContainer);
        jobScroll.setFitToWidth(true);
        jobScroll.setPrefHeight(300);
        jobScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        VBox panel = new VBox(10, title, controlsBar, tableHeader, jobScroll, errorLabel);
        panel.getStyleClass().add("panel");
        panel.setMinWidth(0);
        return panel;
    }

    private HBox createModeSelector() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        for (MultiprogrammingEngine.ExecutionMode mode : MultiprogrammingEngine.ExecutionMode.values()) {
            Button tab = new Button(mode.getDisplayName());
            tab.getStyleClass().add("algo-tab");
            if (mode == selectedMode) tab.getStyleClass().add("algo-tab-active");
            tab.setOnAction(e -> selectMode(mode));
            modeTabs.add(tab);
            bar.getChildren().add(tab);
        }

        Label qLabel = new Label("Quantum:");
        qLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        quantumField.setPrefWidth(55);
        quantumField.setPromptText("0.2");
        Label qUnit = new Label("s");
        qUnit.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        quantumBox.setAlignment(Pos.CENTER_LEFT);
        quantumBox.getChildren().addAll(qLabel, quantumField, qUnit);
        quantumBox.setVisible(false);
        quantumBox.setManaged(false);

        bar.getChildren().add(quantumBox);
        return bar;
    }

    private void selectMode(MultiprogrammingEngine.ExecutionMode mode) {
        selectedMode = mode;
        for (int i = 0; i < modeTabs.size(); i++) {
            Button b = modeTabs.get(i);
            b.getStyleClass().remove("algo-tab-active");
            if (MultiprogrammingEngine.ExecutionMode.values()[i] == mode) {
                b.getStyleClass().add("algo-tab-active");
            }
        }
        boolean isRR = (mode == MultiprogrammingEngine.ExecutionMode.ROUND_ROBIN);
        quantumBox.setVisible(isRR);
        quantumBox.setManaged(isRR);
        runSimulation();
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        return l;
    }

    private VBox createRightPanel() {
        Label sysTitle = new Label("System Resources");
        sysTitle.getStyleClass().add("panel-title");
        sysTitle.setStyle("-fx-font-size: 18px;");

        HBox donutBox = new HBox(donutCanvas);
        donutBox.setAlignment(Pos.CENTER);
        donutBox.setMinWidth(180);

        VBox barsBox = new VBox(12,
                createResourceBarRow("CPU Utilization", "#2dd4bf", cpuBar, cpuBarLabel),
                createResourceBarRow("Min CPU/Job", "#f59e0b", shareBar, shareBarLabel),
                createResourceBarRow("Avg Turnaround", "#38bdf8", tatBar, tatBarLabel)
        );
        barsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(barsBox, Priority.ALWAYS);

        VBox sysResPanel = new VBox(10, sysTitle, new HBox(14, donutBox, barsBox));
        sysResPanel.getStyleClass().add("panel");

        Label mapTitle = new Label("Job Burst Distribution Map");
        mapTitle.getStyleClass().add("panel-title");
        mapTitle.setStyle("-fx-font-size: 16px;");

        Label mapSub = new Label("Proportional CPU service demand across submitted jobs.");
        mapSub.getStyleClass().add("panel-subtitle");

        burstMapCanvas.widthProperty().addListener((obs, o, n) -> {
            if (currentOutput != null) drawBurstMap(currentOutput);
        });

        VBox mapPanel = new VBox(6, mapTitle, mapSub, burstMapCanvas);
        mapPanel.getStyleClass().add("panel");
        burstMapCanvas.widthProperty().bind(mapPanel.widthProperty().subtract(40));

        return new VBox(14, sysResPanel, mapPanel);
    }

    private VBox createTracePanel() {
        Label title = new Label("Processor-Sharing Event Trace");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 16px;");

        HBox header = new HBox(8,
                colHeader("time", 75),
                colHeader("event", 120),
                colHeader("nj", 50),
                colHeader("%cpu/job", 85),
                colHeader("et", 75),
                colHeader("hw/job", 80),
                colHeader("job# & time left", 380)
        );
        header.setStyle("-fx-padding: 4 8; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        VBox panel = new VBox(8, title, header, traceContainer);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private VBox createResourceBarRow(String name, String colorHex, ProgressBar bar, Label valueLabel) {
        Label dot = new Label("● ");
        dot.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 12px;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-min-width: 85;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        valueLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-font-weight: bold;");

        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: " + colorHex + ";");

        return new VBox(4, new HBox(dot, nameLabel, sp, valueLabel), bar);
    }

    // Exact default dataset from the Excel image
    private void populateDefaultData() {
        jobRows.clear();
        jobListContainer.getChildren().clear();

        addJobRow("j1", 10.0, 0.3);
        addJobRow("j2", 10.2, 0.5);
        addJobRow("j3", 10.4, 0.1);
        addJobRow("j4", 10.5, 0.4);
        addJobRow("j5", 10.8, 0.1);
    }

    private void addJobRow(String pid, double ar, double bt) {
        String id = pid.isEmpty() ? "j" + (jobRows.size() + 1) : pid;
        JobInputRow row = new JobInputRow(id, ar, bt);
        jobRows.add(row);
        jobListContainer.getChildren().add(row.getNode());
    }

    private void runSimulation() {
        errorLabel.setText("");
        List<ProgramProcess> requests = new ArrayList<>();

        for (JobInputRow row : jobRows) {
            try {
                double ar = Double.parseDouble(row.getArText().trim());
                double bt = Double.parseDouble(row.getBtText().trim());
                requests.add(new ProgramProcess(row.getPid(), ar, bt));
            } catch (NumberFormatException e) {
                errorLabel.setText(row.getPid() + ": ar and bt must be valid numbers.");
                return;
            }
        }

        List<String> errors = engine.validate(requests);
        if (!errors.isEmpty()) {
            errorLabel.setText(String.join("\n", errors));
            return;
        }

        double quantum = 0.2;
        if (selectedMode == MultiprogrammingEngine.ExecutionMode.ROUND_ROBIN) {
            try {
                quantum = Double.parseDouble(quantumField.getText().trim());
                if (quantum <= 0) {
                    errorLabel.setText("Time Quantum must be positive (> 0).");
                    return;
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Time Quantum must be a valid number.");
                return;
            }
        }

        session.setSimulationState(SimulationState.RUNNING);
        currentOutput = engine.simulate(requests, selectedMode, quantum);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();
        session.setActiveProcesses(currentOutput.getTotalJobs());

        List<String> names = currentOutput.getJobs().stream().map(ProgramProcess::getPid).toList();
        session.initDefaultProcesses(names);

        updateView(currentOutput);
    }

    private void updateView(MultiprogrammingOutput output) {
        jobsCountValue.setText(String.format("%02d", output.getTotalJobs()));
        if (selectedMode == MultiprogrammingEngine.ExecutionMode.ROUND_ROBIN) {
            jobsCountSub.setText("Quantum: " + quantumField.getText().trim() + "s");
        } else {
            jobsCountSub.setText("Peak concurrency (nj): " + output.getMaxConcurrency());
        }

        totalBurstValue.setText(String.format("%.2f", output.getTotalBurstWork()));
        finishTimeValue.setText(String.format("%.2f", output.getEndTime()));
        cpuUtilValue.setText(String.format("%.1f%%", output.getCpuUtilization()));
        cpuUtilSub.setText(output.getCpuUtilization() >= 90.0 ? "✓ High CPU utilization" : "Idle gaps present");

        List<ProgramProcess> res = output.getJobs();
        for (int i = 0; i < jobRows.size() && i < res.size(); i++) {
            jobRows.get(i).updateResult(res.get(i));
        }

        double minShare = output.getMaxConcurrency() > 0 ? (100.0 / output.getMaxConcurrency()) : 0.0;
        drawDonutChart(output.getCpuUtilization());
        cpuBar.setProgress(output.getCpuUtilization() / 100.0);
        cpuBarLabel.setText(String.format("%.1f%%", output.getCpuUtilization()));

        shareBar.setProgress(minShare / 100.0);
        shareBarLabel.setText(String.format("%.1f%%", minShare));

        double span = Math.max(0.01, output.getEndTime() - output.getStartTime());
        tatBar.setProgress(Math.min(1.0, output.getAvgTurnaround() / span));
        tatBarLabel.setText(String.format("%.2f", output.getAvgTurnaround()));

        drawBurstMap(output);

        traceContainer.getChildren().clear();
        for (String[] r : output.getTraceRows()) {
            HBox line = new HBox(8,
                    traceCell(r[0], 75, "#f8fafc"),
                    traceCell(r[1], 120, r[1].contains(" t") ? "#f59e0b" : "#2dd4bf"),
                    traceCell(r[2], 50, "#94a3b8"),
                    traceCell(r[3], 85, "#38bdf8"),
                    traceCell(r[4], 75, "#94a3b8"),
                    traceCell(r[5], 80, "#94a3b8"),
                    traceCell(r[6], 380, "#e2e8f0")
            );
            line.setStyle("-fx-padding: 5 8; -fx-background-color: #161b22; -fx-background-radius: 5;");
            traceContainer.getChildren().add(line);
        }
    }

    private Label traceCell(String text, double width, String color) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        return l;
    }

    private void drawDonutChart(double cpuPercent) {
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        double w = donutCanvas.getWidth(), h = donutCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        double cx = w / 2, cy = h / 2, radius = Math.min(w, h) / 2 - 10;

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(18);
        gc.strokeArc(cx - radius + 9, cy - radius + 9, (radius - 9) * 2, (radius - 9) * 2, 0, 360, ArcType.OPEN);

        gc.setStroke(Color.web("#2dd4bf"));
        gc.strokeArc(cx - radius + 9, cy - radius + 9, (radius - 9) * 2, (radius - 9) * 2, 90, -(cpuPercent / 100.0) * 360, ArcType.OPEN);

        gc.setFill(Color.web("#f8fafc"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        gc.fillText(String.format("%.1f%%", cpuPercent), cx - 32, cy + 4);

        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.fillText("CPU LOAD", cx - 26, cy + 18);
    }

    private void drawBurstMap(MultiprogrammingOutput output) {
        GraphicsContext gc = burstMapCanvas.getGraphicsContext2D();
        double w = burstMapCanvas.getWidth(), h = burstMapCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double total = output.getTotalBurstWork();
        if (total <= 0) return;

        double currentX = 0, barHeight = h - 16, y = 8;
        List<ProgramProcess> jobs = output.getJobs();

        for (int i = 0; i < jobs.size(); i++) {
            ProgramProcess j = jobs.get(i);
            double blockWidth = (j.getBurstTime() / total) * w;
            gc.setFill(JOB_COLORS[i % JOB_COLORS.length]);
            gc.fillRoundRect(currentX, y, blockWidth, barHeight, 4, 4);

            gc.setFill(Color.web("#0d1117"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            if (blockWidth > 28) gc.fillText(j.getPid(), currentX + blockWidth / 2 - 8, y + barHeight / 2 + 4);
            currentX += blockWidth;
        }
    }

    private void removeJob(String pid) {
        jobRows.removeIf(r -> r.getPid().equalsIgnoreCase(pid));
        jobListContainer.getChildren().setAll(jobRows.stream().map(JobInputRow::getNode).toList());
        session.killProcess(pid);
        if (!jobRows.isEmpty()) runSimulation();
    }

    private class JobInputRow {
        private final String pid;
        private final TextField arField;
        private final TextField btField;
        private final Label finishLabel = new Label("—");
        private final Label tatLabel = new Label("—");
        private final HBox node;

        public JobInputRow(String pid, double ar, double bt) {
            this.pid = pid;

            Label pLabel = new Label(pid);
            pLabel.setPrefWidth(70);
            pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

            arField = new TextField(String.valueOf(ar));
            arField.setPrefWidth(80);
            HBox arBox = new HBox(arField);
            arBox.setPrefWidth(100);

            btField = new TextField(String.valueOf(bt));
            btField.setPrefWidth(80);
            HBox btBox = new HBox(btField);
            btBox.setPrefWidth(100);

            finishLabel.setPrefWidth(95);
            finishLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e2e8f0;");

            tatLabel.setPrefWidth(90);
            tatLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            Button termBtn = new Button("✕ Term");
            termBtn.setPrefWidth(75);
            termBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-border-radius: 4; -fx-text-fill: #ef4444; -fx-font-size: 10px; -fx-cursor: hand;");
            termBtn.setOnAction(e -> removeJob(pid));

            node = new HBox(8, pLabel, arBox, btBox, finishLabel, tatLabel, termBtn);
            node.setAlignment(Pos.CENTER_LEFT);
            node.setStyle("-fx-padding: 6 8; -fx-background-color: #161b22; -fx-background-radius: 6;");
        }

        public void updateResult(ProgramProcess p) {
            finishLabel.setText(String.format("%.2f", p.getFinishTime()));
            tatLabel.setText(String.format("%.2f", p.getTurnaroundTime()));
        }

        public String getPid() { return pid; }
        public String getArText() { return arField.getText(); }
        public String getBtText() { return btField.getText(); }
        public double getArVal() {
            try { return Double.parseDouble(arField.getText().trim()); } catch (Exception e) { return 10.0; }
        }
        public HBox getNode() { return node; }
    }
}
