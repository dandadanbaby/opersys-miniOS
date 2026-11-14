package com.beastos.disk.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.disk.engine.DiskScheduler;
import com.beastos.disk.engine.DiskScheduler.Algorithm;
import com.beastos.disk.model.DiskSchedulingOutput;
import com.beastos.disk.model.SeekStep;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Disk Scheduling module view for BEAST OS.
 * Matches design: 6 algorithm tabs, service order flow, 2D seek trajectory,
 * step-by-step calculation table.
 */
public class DiskSchedulingView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final DiskScheduler scheduler = new DiskScheduler();

    // Algorithm selection
    private Algorithm selectedAlgorithm = Algorithm.SSTF;
    private final List<Button> algoTabs = new ArrayList<>();

    // Input
    private final TextField initialPosField = new TextField("53");
    private final TextField requestQueueField = new TextField();
    private final TextField cylinderLimitField = new TextField("200");
    private final CheckBox directionUpCheck = new CheckBox("Direction: Upward (→ higher cylinders)");

    // Stat cards
    private final Label headPosValue = new Label("53");
    private final Label cylLimitValue = new Label("200");
    private final Label totalMoveValue = new Label("—");
    private final Label totalMoveSub = new Label(" ");
    private final Label avgSeekValue = new Label("—");

    // Results
    private final HBox sequenceFlowBox = new HBox(4);
    private final Canvas trajectoryCanvas = new Canvas(500, 340);
    private final TableView<SeekStep> seekTable = new TableView<>();
    private final Label totalCalcLabel = new Label();
    private final Label errorLabel = new Label();

    public DiskSchedulingView() {
        VBox content = new VBox(20);

        content.getChildren().add(createHeader());
        content.getChildren().add(createStatCards());
        content.getChildren().add(createMainArea());
        content.getChildren().add(createSeekTablePanel());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Default values from design reference
        initialPosField.setText("53");
        cylinderLimitField.setText("199");
        requestQueueField.setText("98, 183, 37, 122, 14, 124, 65, 67");
        directionUpCheck.setSelected(true);
        directionUpCheck.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        // Select SSTF matching disk-scheduling.png and run simulation
        selectAlgorithm("SSTF");
        runScheduling();
    }

    // ================================================================
    //  Header
    // ================================================================
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");
        Label title = new Label("Disk Scheduling");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");
        Label desc = new Label("Analyze disk request queue optimization and seek head trajectory over time.");
        desc.getStyleClass().add("content-description");

        Button newSimBtn = new Button("New Simulation");
        newSimBtn.getStyleClass().add("btn-primary");
        newSimBtn.setOnAction(e -> resetAll());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox titleRow = new HBox(title, sp, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(2, tag, titleRow, subtitle, desc);
    }

    // ================================================================
    //  Stat Cards
    // ================================================================
    private HBox createStatCards() {
        VBox c1 = buildStatCard("ACTIVE PROCESS / INITIAL HEAD", headPosValue, "Starting cylinder", "#3b82f6");
        VBox c2 = buildStatCard("CYLINDER LIMIT", cylLimitValue, "Max physical track", "#2dd4bf");
        VBox c3 = buildStatCard("TOTAL HEAD MOVEMENT", totalMoveValue, "Cylinders traversed", "#22c55e");
        totalMoveSub.getStyleClass().add("stat-card-description");
        VBox c4 = buildStatCard("AVG SEEK DISTANCE", avgSeekValue, "Tracks per seek step", "#2dd4bf");

        HBox row = new HBox(14, c1, c2, c3, c4);
        row.setAlignment(Pos.TOP_LEFT);
        for (VBox c : new VBox[]{c1, c2, c3, c4}) HBox.setHgrow(c, Priority.ALWAYS);
        return row;
    }

    private VBox buildStatCard(String label, Label value, String descText, String accentColor) {
        Label l = new Label(label);
        l.getStyleClass().add("stat-card-label");
        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4; -fx-min-width: 8; -fx-min-height: 8; -fx-max-width: 8; -fx-max-height: 8;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox top = new HBox(l, sp, dot);
        top.setAlignment(Pos.CENTER);
        value.getStyleClass().add("stat-card-value");
        Label d = new Label(descText);
        d.getStyleClass().add("stat-card-description");
        d.setStyle("-fx-text-fill: " + accentColor + ";");
        VBox card = new VBox(4, top, value, d);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ================================================================
    //  Main Area: Left (input + sequence flow) | Right (2D trajectory)
    // ================================================================
    private HBox createMainArea() {
        VBox leftPanel = createInputPanel();
        VBox rightPanel = createTrajectoryPanel();

        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        leftPanel.setMinWidth(400);
        leftPanel.setPrefWidth(500);

        return new HBox(14, leftPanel, rightPanel);
    }

    // ================================================================
    //  Input Panel (left side)
    // ================================================================
    private VBox createInputPanel() {
        // Title + algo tabs
        Label panelTitle = new Label("Track Request Queue & Service Order");
        panelTitle.getStyleClass().add("panel-title");
        panelTitle.setStyle("-fx-font-size: 18px;");

        HBox tabBar = createAlgorithmTabs();

        // Input fields
        Label ipLabel = new Label("Initial Position:");
        ipLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        initialPosField.setPrefWidth(80);
        initialPosField.setPromptText("53");

        Label clLabel = new Label("Cylinder Limit:");
        clLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        cylinderLimitField.setPrefWidth(80);
        cylinderLimitField.setPromptText("200");

        HBox inputRow1 = new HBox(8, ipLabel, initialPosField, clLabel, cylinderLimitField);
        inputRow1.setAlignment(Pos.CENTER_LEFT);

        requestQueueField.setPromptText("98, 183, 37, 122, 14, 124, 65, 67");
        requestQueueField.setPrefWidth(320);

        HBox inputRow2 = new HBox(8, requestQueueField);
        inputRow2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(requestQueueField, Priority.ALWAYS);

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        // Service Order Sequence Flow label
        Label flowTitle = new Label("Service Order Sequence Flow");
        flowTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        ScrollPane flowScroll = new ScrollPane(sequenceFlowBox);
        flowScroll.setFitToHeight(true);
        flowScroll.setPrefHeight(100);
        flowScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sequenceFlowBox.setAlignment(Pos.CENTER_LEFT);
        sequenceFlowBox.setPadding(new Insets(8));

        // Buttons
        Button restartBtn = new Button("⟳ Restart Simulation");
        restartBtn.getStyleClass().add("btn-outline");
        restartBtn.setOnAction(e -> resetAll());

        Button confirmBtn = new Button("Confirm");
        confirmBtn.getStyleClass().add("btn-accent");
        confirmBtn.setOnAction(e -> runScheduling());

        HBox btnRow = new HBox(12, restartBtn, confirmBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, panelTitle, tabBar, inputRow1, inputRow2,
                directionUpCheck, errorLabel, flowTitle, flowScroll, btnRow);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private HBox createAlgorithmTabs() {
        String[] algorithms = {"FCFS", "SSTF", "SCAN", "C-SCAN", "LOOK", "C-LOOK"};
        HBox tabBar = new HBox(6);
        tabBar.setAlignment(Pos.CENTER_LEFT);

        for (String name : algorithms) {
            Button tab = new Button(name);
            tab.getStyleClass().add("algo-tab");
            if (name.equals("SSTF")) tab.getStyleClass().add("algo-tab-active");
            tab.setOnAction(e -> selectAlgorithm(name));
            algoTabs.add(tab);
            tabBar.getChildren().add(tab);
        }
        return tabBar;
    }

    private void selectAlgorithm(String name) {
        selectedAlgorithm = switch (name) {
            case "FCFS"   -> Algorithm.FCFS;
            case "SSTF"   -> Algorithm.SSTF;
            case "SCAN"   -> Algorithm.SCAN;
            case "C-SCAN" -> Algorithm.C_SCAN;
            case "LOOK"   -> Algorithm.LOOK;
            case "C-LOOK" -> Algorithm.C_LOOK;
            default       -> Algorithm.FCFS;
        };
        for (Button tab : algoTabs) {
            tab.getStyleClass().remove("algo-tab-active");
            if (tab.getText().equals(name)) tab.getStyleClass().add("algo-tab-active");
        }
        // Show/hide direction checkbox (only relevant for SCAN, C-SCAN, LOOK, C-LOOK)
        boolean needsDirection = selectedAlgorithm != Algorithm.FCFS && selectedAlgorithm != Algorithm.SSTF;
        directionUpCheck.setVisible(needsDirection);
        directionUpCheck.setManaged(needsDirection);
    }

    // ================================================================
    //  2D Trajectory Panel (right side)
    // ================================================================
    private VBox createTrajectoryPanel() {
        Label title = new Label("2D Disk Head Seek Trajectory");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");

        trajectoryCanvas.widthProperty().addListener((o, a, b) -> drawEmptyTrajectory());

        VBox panel = new VBox(8, title, trajectoryCanvas);
        panel.getStyleClass().add("panel");

        trajectoryCanvas.widthProperty().bind(panel.widthProperty().subtract(40));

        return panel;
    }

    // ================================================================
    //  Step-by-Step Seek Distance Calculation Table
    // ================================================================
    @SuppressWarnings("unchecked")
    private VBox createSeekTablePanel() {
        Label title = new Label("Step-by-Step Seek Distance Calculation Table");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");

        // Columns
        TableColumn<SeekStep, Number> stepCol = new TableColumn<>("Step");
        stepCol.setCellValueFactory(r -> new SimpleIntegerProperty(r.getValue().getStepNumber()));
        stepCol.setPrefWidth(60);

        TableColumn<SeekStep, String> transCol = new TableColumn<>("Sequence Transition");
        transCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getTransitionLabel()));
        transCol.setPrefWidth(140);

        TableColumn<SeekStep, Number> prevCol = new TableColumn<>("Previous Cyl");
        prevCol.setCellValueFactory(r -> new SimpleIntegerProperty(r.getValue().getPreviousCylinder()));
        prevCol.setPrefWidth(100);

        TableColumn<SeekStep, Number> targetCol = new TableColumn<>("Target Cyl");
        targetCol.setCellValueFactory(r -> new SimpleIntegerProperty(r.getValue().getTargetCylinder()));
        targetCol.setPrefWidth(90);

        TableColumn<SeekStep, String> distCol = new TableColumn<>("Calculated Distance\n(Absolute Difference)");
        distCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getSeekDistance())));
        distCol.setPrefWidth(160);

        seekTable.getColumns().addAll(stepCol, transCol, prevCol, targetCol, distCol);
        seekTable.setPlaceholder(new Label("Run a disk scheduling simulation to see step-by-step results"));
        seekTable.setPrefHeight(260);
        seekTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Style the transition column with teal
        transCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: #2dd4bf; -fx-font-weight: bold;"); }
            }
        });

        totalCalcLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-family: 'Consolas', monospace;");
        totalCalcLabel.setWrapText(true);

        VBox panel = new VBox(10, title, seekTable, totalCalcLabel);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // ================================================================
    //  Simulation Execution
    // ================================================================
    private void runScheduling() {
        errorLabel.setText("");

        // Parse initial position
        String ipText = initialPosField.getText().trim();
        if (ipText.isEmpty()) { errorLabel.setText("Initial position is required."); return; }
        int initialPos;
        try { initialPos = Integer.parseInt(ipText); }
        catch (NumberFormatException e) { errorLabel.setText("Initial position must be a valid integer."); return; }

        // Parse cylinder limit
        String clText = cylinderLimitField.getText().trim();
        if (clText.isEmpty()) { errorLabel.setText("Cylinder limit is required."); return; }
        int cylLimit;
        try { cylLimit = Integer.parseInt(clText); }
        catch (NumberFormatException e) { errorLabel.setText("Cylinder limit must be a valid integer."); return; }

        // Parse request queue
        String queueText = requestQueueField.getText().trim();
        if (queueText.isEmpty()) { errorLabel.setText("Request queue is required."); return; }
        // Remove brackets if present
        queueText = queueText.replaceAll("[\\[\\]()]", "");
        String[] parts = queueText.split("[,\\s]+");
        int[] queue;
        try {
            List<Integer> parsed = new ArrayList<>();
            for (String p : parts) {
                String s = p.trim();
                if (!s.isEmpty()) parsed.add(Integer.parseInt(s));
            }
            queue = parsed.stream().mapToInt(Integer::intValue).toArray();
        } catch (NumberFormatException e) {
            errorLabel.setText("Request queue must contain valid integers separated by commas.");
            return;
        }

        // Validate
        List<String> errors = scheduler.validate(queue, initialPos, cylLimit);
        if (!errors.isEmpty()) { errorLabel.setText(String.join("\n", errors)); return; }

        boolean dirUp = directionUpCheck.isSelected();

        // Run
        session.setSimulationState(SimulationState.RUNNING);
        DiskSchedulingOutput output = scheduler.schedule(queue, initialPos, cylLimit, selectedAlgorithm, dirUp);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        // Update stat cards
        headPosValue.setText(String.valueOf(initialPos));
        cylLimitValue.setText(String.valueOf(cylLimit));
        totalMoveValue.setText(String.valueOf(output.getTotalSeekDistance()));
        totalMoveSub.setText("Cylinders");
        avgSeekValue.setText(String.format("%.1f", output.getAverageSeekDistance()));

        // Build sequence flow
        buildSequenceFlow(initialPos, output);

        // Draw 2D trajectory
        drawTrajectory(output);

        // Update seek table
        seekTable.setItems(FXCollections.observableArrayList(output.getSteps()));

        // Total distance calculation string
        StringBuilder calc = new StringBuilder("Total Distance Calculation: ");
        for (int i = 0; i < output.getSteps().size(); i++) {
            if (i > 0) calc.append(" + ");
            calc.append(output.getSteps().get(i).getSeekDistance());
        }
        calc.append(" = ").append(output.getTotalSeekDistance()).append(" cylinders");
        calc.append(" | Average Seek: ").append(String.format("%.1f", output.getAverageSeekDistance()))
                .append(" cylinders");
        totalCalcLabel.setText(calc.toString());
    }

    // ================================================================
    //  Service Order Sequence Flow — circular nodes with arrows
    // ================================================================
    private void buildSequenceFlow(int initialPos, DiskSchedulingOutput output) {
        sequenceFlowBox.getChildren().clear();

        // Add initial position node
        sequenceFlowBox.getChildren().add(createFlowNode(String.valueOf(initialPos), true));

        for (int cyl : output.getSeekSequence()) {
            // Arrow
            Label arrow = new Label("→");
            arrow.setStyle("-fx-font-size: 18px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
            sequenceFlowBox.getChildren().add(arrow);

            // Cylinder node
            sequenceFlowBox.getChildren().add(createFlowNode(String.valueOf(cyl), false));
        }
    }

    private StackPane createFlowNode(String text, boolean isInitial) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        lbl.setAlignment(Pos.CENTER);

        StackPane node = new StackPane(lbl);
        node.setMinSize(50, 50);
        node.setPrefSize(50, 50);
        node.setMaxSize(50, 50);
        if (isInitial) {
            node.setStyle("-fx-background-color: #14716a; -fx-background-radius: 25; " +
                    "-fx-border-color: #2dd4bf; -fx-border-radius: 25; -fx-border-width: 2;");
        } else {
            node.setStyle("-fx-background-color: #1c2333; -fx-background-radius: 25; " +
                    "-fx-border-color: #2dd4bf; -fx-border-radius: 25; -fx-border-width: 2;");
        }
        return node;
    }

    // ================================================================
    //  2D Disk Head Seek Trajectory (Canvas)
    // ================================================================
    private void drawEmptyTrajectory() {
        GraphicsContext gc = trajectoryCanvas.getGraphicsContext2D();
        double w = trajectoryCanvas.getWidth();
        double h = trajectoryCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.web("#64748b"));
        gc.setFont(Font.font("Segoe UI", 14));
        gc.fillText("Configure request queue and click Confirm", 30, h / 2);
    }

    private void drawTrajectory(DiskSchedulingOutput output) {
        GraphicsContext gc = trajectoryCanvas.getGraphicsContext2D();
        double w = trajectoryCanvas.getWidth();
        double h = trajectoryCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        if (output.getSeekSequence().isEmpty()) { drawEmptyTrajectory(); return; }

        double marginL = 50;
        double marginR = 30;
        double marginT = 30;
        double marginB = 40;
        double chartW = w - marginL - marginR;
        double chartH = h - marginT - marginB;

        int maxCyl = output.getCylinderLimit();
        int totalSteps = output.getSeekSequence().size();

        // Build the full sequence including initial position
        List<Integer> fullSeq = new ArrayList<>();
        fullSeq.add(output.getInitialPosition());
        fullSeq.addAll(output.getSeekSequence());

        // Draw grid lines and axes
        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1);

        // X-axis grid + labels (Cylinder Number)
        int xTickCount = 8;
        gc.setFill(Color.web("#64748b"));
        gc.setFont(Font.font("Segoe UI", 10));
        for (int i = 0; i <= xTickCount; i++) {
            double x = marginL + (chartW * i / xTickCount);
            int cylVal = (int) Math.round((double) maxCyl * i / xTickCount);
            gc.strokeLine(x, marginT, x, marginT + chartH);
            gc.fillText(String.valueOf(cylVal), x - 8, marginT + chartH + 15);
        }

        // Y-axis label
        gc.save();
        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.translate(12, marginT + chartH / 2);
        gc.rotate(-90);
        gc.fillText("Sequence Step", -40, 0);
        gc.restore();

        // X-axis label
        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(Font.font("Segoe UI", 11));
        gc.fillText("Cylinder Number", marginL + chartW / 2 - 40, h - 5);

        // Y-axis labels (step numbers, descending like the design)
        gc.setFill(Color.web("#64748b"));
        gc.setFont(Font.font("Segoe UI", 10));
        for (int i = 0; i < fullSeq.size(); i++) {
            double y = marginT + (chartH * i / Math.max(fullSeq.size() - 1, 1));
            gc.fillText(String.valueOf(i), marginL - 15, y + 4);
        }

        // Draw trajectory line
        gc.setStroke(Color.web("#2dd4bf"));
        gc.setLineWidth(2);

        double[] xPoints = new double[fullSeq.size()];
        double[] yPoints = new double[fullSeq.size()];

        for (int i = 0; i < fullSeq.size(); i++) {
            xPoints[i] = marginL + (chartW * fullSeq.get(i) / maxCyl);
            yPoints[i] = marginT + (chartH * i / Math.max(fullSeq.size() - 1, 1));
        }

        for (int i = 0; i < fullSeq.size() - 1; i++) {
            gc.strokeLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }

        // Draw data points and labels
        gc.setFont(Font.font("Segoe UI", 10));
        for (int i = 0; i < fullSeq.size(); i++) {
            double px = xPoints[i];
            double py = yPoints[i];

            // Point circle
            gc.setFill(Color.web("#2dd4bf"));
            gc.fillOval(px - 4, py - 4, 8, 8);

            // Label
            gc.setFill(Color.web("#94a3b8"));
            gc.fillText("(" + fullSeq.get(i) + ")", px + 6, py - 4);
        }
    }

    // ================================================================
    //  Reset
    // ================================================================
    private void resetAll() {
        initialPosField.setText("53");
        cylinderLimitField.setText("200");
        requestQueueField.setText("98, 183, 37, 122, 14, 124, 65, 67");
        directionUpCheck.setSelected(true);
        headPosValue.setText("53");
        cylLimitValue.setText("200");
        totalMoveValue.setText("—");
        totalMoveSub.setText(" ");
        avgSeekValue.setText("—");
        sequenceFlowBox.getChildren().clear();
        seekTable.getItems().clear();
        totalCalcLabel.setText("");
        errorLabel.setText("");
        drawEmptyTrajectory();
        session.setSimulationState(SimulationState.IDLE);
    }
}
