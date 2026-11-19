package com.beastos.cpu.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.cpu.engine.*;
import com.beastos.cpu.model.Process;
import com.beastos.cpu.model.SchedulingOutput;
import com.beastos.cpu.model.SchedulingResult;
import com.beastos.validation.InputValidator;
import com.beastos.validation.ValidationResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/*
 * Team notes on our CPU Scheduling screen:
 * Basically, this is where we visualize how different CPU scheduling algorithms handle processes over time!
 * We designed the UI with stat cards at the top for quick averages, an editable process table
 * on the left where users can test different algorithms, and the visual Gantt chart + detailed
 * calculation metrics table on the right.
 */
public class CpuSchedulingView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();

    // tracks which algorithm tab is selected (we start on FCFS by default)
    private String selectedAlgorithm = "FCFS";
    private final List<Button> algoTabs = new ArrayList<>();

    // input controls: container for our dynamic process rows, and the quantum box for Round Robin
    private final VBox processInputRows = new VBox(6);
    private final TextField quantumField = new TextField("4");
    private final HBox quantumConfig = new HBox(8);

    // stat card labels so the user can quickly see overall performance metrics without hunting for them
    private final Label activeProcessesValue = new Label("00");
    private final Label avgWaitingValue = new Label("0.00");
    private final Label avgTurnaroundValue = new Label("0.00");
    private final Label cpuUtilValue = new Label("0.0%");

    // results components: custom canvas for the Gantt chart, results table, and average metrics footer
    private final Canvas ganttCanvas = new Canvas(580, 220);
    private final TableView<SchedulingResult> resultsTable = new TableView<>();
    private final Label avgWtFooter = new Label("Average Waiting Time: 0.00 ms");
    private final Label avgTatFooter = new Label("Average Turnaround Time: 0.00 ms");
    private final Label errorLabel = new Label();

    public CpuSchedulingView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        content.getChildren().add(createHeader());
        content.getChildren().add(createStatCards());
        content.getChildren().add(createMainArea());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // we load up 4 default processes from our design reference so the app has data ready right away
        addProcessRow("P1", "0", "8", "3");
        addProcessRow("P2", "1", "4", "1");
        addProcessRow("P3", "2", "9", "4");
        addProcessRow("P4", "3", "5", "2");

        // we select Round Robin with quantum 4 and run it on launch so the screen isn't blank
        selectAlgorithm("Round Robin");
        quantumField.setText("4");
        runSimulation();
    }

    // builds the top header bar with the title, description, and "New Simulation" reset button
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("CPU Scheduling");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Label desc = new Label("Analyze scheduling order and calculate relevant CPU process metrics over a timeline.");
        desc.getStyleClass().add("content-description");

        Button newSimBtn = new Button("New Simulation");
        newSimBtn.getStyleClass().add("btn-primary");
        newSimBtn.setOnAction(e -> resetSimulation());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(title, spacer, newSimBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(2, tag, titleRow, subtitle, desc);
        return header;
    }

    // creates the 4 summary cards up top: active processes, average wait, turnaround time, and CPU utilization
    private HBox createStatCards() {
        VBox card1 = buildStatCard("ACTIVE PROCESSES", activeProcessesValue, "", "+2 this session", "#22c55e");
        VBox card2 = buildStatCard("AVG WAITING TIME", avgWaitingValue, "ms", "Average queue wait", "#2dd4bf");
        VBox card3 = buildStatCard("AVG TURNAROUND TIME", avgTurnaroundValue, "ms", "Dispatch to completion", "#3b82f6");
        VBox card4 = buildStatCard("CPU UTILIZATION", cpuUtilValue, "", "Optimal throughput", "#22c55e");

        HBox row = new HBox(14, card1, card2, card3, card4);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        return row;
    }

    // helper to build a nice styled stat card with a colored accent badge
    private VBox buildStatCard(String labelText, Label valueLabel, String unitText, String descText, String colorHex) {
        Label cardLabel = new Label(labelText);
        cardLabel.getStyleClass().add("stat-card-label");

        Region badge = new Region();
        badge.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4; -fx-min-width: 8; -fx-min-height: 8; -fx-max-width: 8; -fx-max-height: 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(cardLabel, spacer, badge);
        topRow.setAlignment(Pos.CENTER);

        valueLabel.getStyleClass().add("stat-card-value");

        Label unitLabel = new Label(unitText);
        unitLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        HBox valueRow = new HBox(4, valueLabel, unitLabel);
        valueRow.setAlignment(Pos.BOTTOM_LEFT);

        Label descLabel = new Label(descText);
        descLabel.getStyleClass().add("stat-card-description");
        descLabel.setStyle("-fx-text-fill: " + colorHex + ";");

        VBox card = new VBox(4, topRow, valueRow, descLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // sets up the two-column split layout: inputs on the left, Gantt chart + results on the right
    private HBox createMainArea() {
        VBox leftPanel = createInputPanel();
        VBox rightPanel = createResultsPanel();

        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        leftPanel.setMinWidth(380);
        leftPanel.setPrefWidth(440);
        leftPanel.setMaxWidth(520);

        HBox main = new HBox(14, leftPanel, rightPanel);
        main.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(main, Priority.ALWAYS);
        return main;
    }

    // left panel: where users pick the algorithm, tweak time quantum, and configure processes
    private VBox createInputPanel() {
        // header row inside the panel with title, hints, and "+ Add Process" button
        Label panelTitle = new Label("Process Input Table");
        panelTitle.getStyleClass().add("panel-title");

        Label algoHint = new Label("(FCFS | SJF | Priority | Round Robin)");
        algoHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Button addBtn = new Button("+ Add Process");
        addBtn.getStyleClass().add("btn-outline");
        addBtn.setOnAction(e -> addProcessRow());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(8, panelTitle, algoHint, spacer, addBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // tab buttons to switch between algorithms
        HBox tabBar = createAlgorithmTabs();

        // quantum config box: we only make this visible for Round Robin because the others don't use quantum
        Label configLabel = new Label("Config");
        configLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        Label qLabel = new Label("Time Quantum:");
        qLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        quantumField.setPrefWidth(60);
        quantumField.setPromptText("4");
        Label qUnit = new Label("ms");
        qUnit.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        quantumConfig.getChildren().addAll(configLabel, qLabel, quantumField, qUnit);
        quantumConfig.setAlignment(Pos.CENTER_LEFT);
        quantumConfig.setVisible(false);
        quantumConfig.setManaged(false);

        // table column headers
        HBox colHeaders = createColumnHeaders();

        // container for the dynamic process input rows
        processInputRows.setPadding(new Insets(0));

        // we wrapped the rows in a ScrollPane so the layout doesn't break if someone adds tons of processes lol
        ScrollPane inputScroll = new ScrollPane(processInputRows);
        inputScroll.setFitToWidth(true);
        inputScroll.setPrefHeight(260);
        inputScroll.setMaxHeight(400);
        inputScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // red error label to display validation issues if inputs are invalid or missing
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);

        // confirm button that triggers validation and runs the simulation
        Button confirmBtn = new Button("Confirm");
        confirmBtn.getStyleClass().add("btn-accent");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> runSimulation());

        VBox panel = new VBox(10, titleRow, tabBar, quantumConfig, colHeaders,
                inputScroll, errorLabel, confirmBtn);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(inputScroll, Priority.ALWAYS);
        return panel;
    }

    // creates clickable tabs for each scheduling algorithm we implemented
    private HBox createAlgorithmTabs() {
        String[] algorithms = {"FCFS", "SJF", "Round Robin", "Priority"};
        HBox tabBar = new HBox(8);
        tabBar.setAlignment(Pos.CENTER_LEFT);

        for (String algo : algorithms) {
            Button tab = new Button(algo);
            tab.getStyleClass().add("algo-tab");
            if (algo.equals(selectedAlgorithm)) {
                tab.getStyleClass().add("algo-tab-active");
            }
            tab.setOnAction(e -> selectAlgorithm(algo));
            algoTabs.add(tab);
            tabBar.getChildren().add(tab);
        }
        return tabBar;
    }

    // switches the active tab style and toggles the Round Robin quantum field on or off
    private void selectAlgorithm(String algo) {
        selectedAlgorithm = algo;
        for (Button tab : algoTabs) {
            tab.getStyleClass().remove("algo-tab-active");
            if (tab.getText().equals(algo)) {
                tab.getStyleClass().add("algo-tab-active");
            }
        }
        boolean isRR = "Round Robin".equals(algo);
        quantumConfig.setVisible(isRR);
        quantumConfig.setManaged(isRR);
    }

    // builds column headers for the input table (Process ID, Arrival, Burst, Priority)
    private HBox createColumnHeaders() {
        Label pidH = new Label("Process ID");
        Label atH = new Label("Arrival Time");
        Label btH = new Label("Burst Time");
        Label prH = new Label("Priority");
        Label delH = new Label("");

        String headerStyle = "-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;";
        pidH.setStyle(headerStyle); pidH.setPrefWidth(90);
        atH.setStyle(headerStyle);  atH.setPrefWidth(90);
        btH.setStyle(headerStyle);  btH.setPrefWidth(90);
        prH.setStyle(headerStyle);  prH.setPrefWidth(80);
        delH.setPrefWidth(30);

        HBox headers = new HBox(8, pidH, atH, btH, prH, delH);
        headers.setAlignment(Pos.CENTER_LEFT);
        headers.setPadding(new Insets(4, 0, 4, 0));
        return headers;
    }

    private void addProcessRow() {
        // we find the lowest unused PID number so if you delete a row, it fills in gaps nicely (e.g. P1, P3 -> adds P2)
        int nextPid = 1;
        java.util.Set<Integer> usedPids = new java.util.HashSet<>();
        for (javafx.scene.Node child : processInputRows.getChildren()) {
            if (child instanceof HBox row) {
                TextField[] fields = (TextField[]) row.getUserData();
                if (fields != null) {
                    String pid = fields[0].getText().trim();
                    if (pid.startsWith("P")) {
                        try { usedPids.add(Integer.parseInt(pid.substring(1))); }
                        catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        while (usedPids.contains(nextPid)) nextPid++;
        addProcessRow("P" + nextPid, "", "", "");
    }

    // creates a single editable row with input fields and a delete button
    private void addProcessRow(String pid, String at, String bt, String pr) {

        TextField pidField = new TextField(pid);
        pidField.setPrefWidth(90);

        TextField atField = new TextField(at);
        atField.setPrefWidth(90);
        atField.setPromptText("0");

        TextField btField = new TextField(bt);
        btField.setPrefWidth(90);
        btField.setPromptText("1");

        TextField prField = new TextField(pr);
        prField.setPrefWidth(80);
        prField.setPromptText("0");

        // delete button to remove this row, but we make sure at least 1 row always stays
        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6 2 6;");
        HBox row = new HBox(8, pidField, atField, btField, prField, delBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(new TextField[]{pidField, atField, btField, prField});

        delBtn.setOnAction(e -> {
            if (processInputRows.getChildren().size() > 1) {
                processInputRows.getChildren().remove(row);
            }
        });

        processInputRows.getChildren().add(row);
    }

    // right panel: contains the visual Gantt chart timeline and the detailed calculation results table
    private VBox createResultsPanel() {
        // Gantt chart panel: draws the visual timeline blocks of process execution
        Label ganttTitle = new Label("CPU execution timeline");
        ganttTitle.getStyleClass().add("panel-title");
        Label ganttSubtitle = new Label("Visual Gantt Chart Scheduler");
        ganttSubtitle.getStyleClass().add("panel-subtitle");

        GanttChartRenderer.render(ganttCanvas, null); // empty state before running anything

        VBox ganttPanel = new VBox(8, ganttTitle, ganttSubtitle, ganttCanvas);
        ganttPanel.getStyleClass().add("panel");
        ganttPanel.setMinHeight(280);

        // tricky part: we bind the canvas width to the panel so it resizes smoothly when the user resizes the window
        ganttCanvas.widthProperty().bind(ganttPanel.widthProperty().subtract(40));
        ganttCanvas.widthProperty().addListener((obs, o, n) -> {
            // re-render the Gantt chart with the latest output if available so it doesn't get stretched or disappear
            Object lastResult = ganttPanel.getUserData();
            if (lastResult instanceof SchedulingOutput out) {
                GanttChartRenderer.render(ganttCanvas, out.getGanttChart());
            } else {
                GanttChartRenderer.render(ganttCanvas, null);
            }
        });

        // results table showing exact calculated values for each process
        Label tableTitle = new Label("Scheduling Calculation Results");
        tableTitle.getStyleClass().add("panel-title");

        setupResultsTable();

        // bottom footer displaying the calculated average waiting time and turnaround time
        avgWtFooter.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
        avgTatFooter.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

        Label avgLabel = new Label("Average Calculation:");
        avgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        Label separator = new Label("|");
        separator.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        HBox footer = new HBox(12, avgLabel, avgWtFooter, separator, avgTatFooter);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 0, 0, 0));

        VBox tablePanel = new VBox(8, tableTitle, resultsTable, footer);
        tablePanel.getStyleClass().add("panel");

        VBox rightPanel = new VBox(14, ganttPanel, tablePanel);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);
        return rightPanel;
    }

    // configures all table columns and binds them to our SchedulingResult model properties
    @SuppressWarnings("unchecked")
    private void setupResultsTable() {
        TableColumn<SchedulingResult, String> pidCol = new TableColumn<>("Process");
        pidCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getPid()));
        pidCol.setPrefWidth(70);

        TableColumn<SchedulingResult, String> atCol = new TableColumn<>("Arrival Time");
        atCol.setCellValueFactory(r -> new SimpleStringProperty(formatVal(r.getValue().getArrivalTime())));
        atCol.setPrefWidth(85);

        TableColumn<SchedulingResult, String> btCol = new TableColumn<>("Burst Time");
        btCol.setCellValueFactory(r -> new SimpleStringProperty(formatVal(r.getValue().getBurstTime())));
        btCol.setPrefWidth(75);

        TableColumn<SchedulingResult, String> prCol = new TableColumn<>("Priority");
        prCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getPriority())));
        prCol.setPrefWidth(65);

        TableColumn<SchedulingResult, String> ctCol = new TableColumn<>("Completion\nTime");
        ctCol.setCellValueFactory(r -> new SimpleStringProperty(formatVal(r.getValue().getCompletionTime())));
        ctCol.setPrefWidth(90);

        TableColumn<SchedulingResult, String> tatCol = new TableColumn<>("Turnaround\nTime (TAT)");
        tatCol.setCellValueFactory(r -> new SimpleStringProperty(formatVal(r.getValue().getTurnaroundTime())));
        tatCol.setPrefWidth(100);

        TableColumn<SchedulingResult, String> wtCol = new TableColumn<>("Waiting\nTime (WT)");
        wtCol.setCellValueFactory(r -> new SimpleStringProperty(formatVal(r.getValue().getWaitingTime())));
        wtCol.setPrefWidth(85);

        resultsTable.getColumns().addAll(pidCol, atCol, btCol, prCol, ctCol, tatCol, wtCol);
        resultsTable.setPlaceholder(new Label("Run a simulation to see results"));
        resultsTable.setPrefHeight(200);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // we style the PID column in teal accent color so it stands out nicely in the table
        pidCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
                }
            }
        });
    }

    // basically the heart of the view: validates inputs, creates process models, runs the engine, and updates the UI
    private void runSimulation() {
        errorLabel.setText("");

        // step 1: read all rows from the input table and validate each value
        List<Process> processes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> pids = new ArrayList<>();

        for (int i = 0; i < processInputRows.getChildren().size(); i++) {
            HBox row = (HBox) processInputRows.getChildren().get(i);
            TextField[] fields = (TextField[]) row.getUserData();
            if (fields == null) continue;

            String pid = fields[0].getText().trim();
            String atStr = fields[1].getText().trim();
            String btStr = fields[2].getText().trim();
            String prStr = fields[3].getText().trim();

            // arrival and priority default to 0 if left blank, but burst time and PID are strictly required
            if (atStr.isEmpty()) atStr = "0";
            if (btStr.isEmpty()) { errors.add("Row " + (i + 1) + ": Burst Time is required."); continue; }
            if (prStr.isEmpty()) prStr = "0";
            if (pid.isEmpty()) { errors.add("Row " + (i + 1) + ": Process ID is required."); continue; }

            ValidationResult vPid = InputValidator.requireNonBlank(pid, "Process ID");
            ValidationResult vAt = InputValidator.validateNonNegativeDouble(atStr, "Arrival Time");
            ValidationResult vBt = InputValidator.validatePositiveDouble(btStr, "Burst Time");
            ValidationResult vPr = InputValidator.validateNonNegativeInt(prStr, "Priority");

            if (vPid.hasErrors()) { errors.addAll(vPid.getErrors()); continue; }
            if (vAt.hasErrors()) { errors.addAll(vAt.getErrors()); continue; }
            if (vBt.hasErrors()) { errors.addAll(vBt.getErrors()); continue; }
            if (vPr.hasErrors()) { errors.addAll(vPr.getErrors()); continue; }

            pids.add(pid);
            processes.add(new Process(pid,
                    Double.parseDouble(atStr),
                    Double.parseDouble(btStr),
                    Integer.parseInt(prStr)));
        }

        if (processes.isEmpty()) {
            errorLabel.setText("At least 1 process is required.");
            return;
        }

        // we had to add this check to ensure no duplicate PIDs are entered, otherwise Gantt/results get messed up
        ValidationResult dupCheck = InputValidator.validateNoDuplicates(pids, "Process IDs");
        if (dupCheck.hasErrors()) {
            errorLabel.setText(String.join("\n", dupCheck.getErrors()));
            return;
        }

        if (!errors.isEmpty()) {
            errorLabel.setText(String.join("\n", errors));
            return;
        }

        // if Round Robin is picked, we must make sure time quantum is a valid positive number
        double quantum = 4;
        if ("Round Robin".equals(selectedAlgorithm)) {
            ValidationResult vQ = InputValidator.validatePositiveDouble(
                    quantumField.getText().trim(), "Time Quantum");
            if (vQ.hasErrors()) {
                errorLabel.setText(String.join("\n", vQ.getErrors()));
                return;
            }
            quantum = Double.parseDouble(quantumField.getText().trim());
        }

        // step 2: instantiate whichever scheduling engine the user selected in the tabs
        CpuScheduler scheduler = switch (selectedAlgorithm) {
            case "FCFS" -> new FcfsScheduler();
            case "SJF" -> new SjfScheduler();
            case "Priority" -> new PriorityScheduler();
            case "Round Robin" -> new RoundRobinScheduler(quantum);
            default -> new FcfsScheduler();
        };

        // step 3: run the scheduling algorithm and collect the output
        session.setSimulationState(SimulationState.RUNNING);
        SchedulingOutput output = scheduler.schedule(processes);
        session.setSimulationState(SimulationState.COMPLETED);

        // step 4: update our stat cards with the new average metrics and CPU utilization
        activeProcessesValue.setText(String.format("%02d", output.getProcessCount()));
        avgWaitingValue.setText(String.format("%.2f", output.getAverageWaitingTime()));
        avgTurnaroundValue.setText(String.format("%.2f", output.getAverageTurnaroundTime()));
        cpuUtilValue.setText(String.format("%.1f%%", output.getCpuUtilization()));

        // update the global session state so other modules know what's active
        session.setActiveProcesses(output.getProcessCount());
        session.setCpuUtilization(output.getCpuUtilization());
        session.incrementSimulationsCompleted();
        session.setLastCpuResult(output);

        // step 5: draw the Gantt chart timeline on our canvas
        VBox ganttPanel = (VBox) ganttCanvas.getParent();
        ganttPanel.setUserData(output);
        GanttChartRenderer.render(ganttCanvas, output.getGanttChart());

        // step 6: populate the results table with individual process rows
        ObservableList<SchedulingResult> data = FXCollections.observableArrayList(output.getResults());
        resultsTable.setItems(data);

        // step 7: update the summary footer with final calculated averages
        avgWtFooter.setText(String.format("Average Waiting Time: %.2f ms", output.getAverageWaitingTime()));
        avgTatFooter.setText(String.format("Average Turnaround Time: %.2f ms", output.getAverageTurnaroundTime()));
    }

    // resets everything back to initial state with 4 blank process rows and cleared charts
    private void resetSimulation() {
        processInputRows.getChildren().clear();
        for (int i = 0; i < 4; i++) addProcessRow();

        activeProcessesValue.setText("00");
        avgWaitingValue.setText("0.00");
        avgTurnaroundValue.setText("0.00");
        cpuUtilValue.setText("0.0%");

        resultsTable.getItems().clear();
        GanttChartRenderer.render(ganttCanvas, null);
        VBox ganttPanel = (VBox) ganttCanvas.getParent();
        ganttPanel.setUserData(null);

        avgWtFooter.setText("Average Waiting Time: 0.00 ms");
        avgTatFooter.setText("Average Turnaround Time: 0.00 ms");
        errorLabel.setText("");

        session.setSimulationState(SimulationState.IDLE);
    }

    private static String formatVal(double val) {
        if (Math.abs(val - Math.round(val)) < 1e-6) {
            return String.valueOf((long) Math.round(val));
        } else {
            return String.format(java.util.Locale.US, "%.2f", val);
        }
    }
}
