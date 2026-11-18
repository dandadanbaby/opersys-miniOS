package com.beastos.banker.view;

import com.beastos.banker.engine.BankerEngine;
import com.beastos.banker.model.BankerState;
import com.beastos.banker.model.SafetyResult;
import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/* our banker's algorithm UI screen!
 * basically this is where we visualize dijkstra's banker's algorithm for deadlock avoidance.
 * we designed the layout with quick stat cards up top, the 3 key matrices side-by-side in the middle
 * (allocation, max, and need), and the available resources + step-by-step verification log at the bottom.
 */
public class BankerView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final BankerEngine engine = new BankerEngine();

    // we defaulted to 5 processes and 3 resources (A, B, C) because that matches the exact example from our OS lecture slides
    private int numProcesses = 5;
    private int numResources = 3;

    // spinners so users can test custom matrix sizes instead of just the fixed 5x3 textbook one
    private final Spinner<Integer> processSpinner = new Spinner<>(1, 50, 5);
    private final Spinner<Integer> resourceSpinner = new Spinner<>(1, 26, 3); // capped at 26 because we label resources with letters A through Z

    // 2D grid arrays for the UI fields — need is read-only labels because we calculate Need = Max - Allocation automatically
    private TextField[][] allocationFields;
    private TextField[][] maxFields;
    private Label[][] needLabels;
    private TextField[] availableFields;

    // vertical boxes holding the matrix rows — we wipe and redraw these whenever someone changes the spinner counts
    private final VBox allocationMatrix = new VBox(4);
    private final VBox maxMatrix = new VBox(4);
    private final VBox needMatrix = new VBox(4);
    private final HBox availableRow = new HBox(8);

    // top stat card labels so the user sees whether the system is safe and the sequence without scrolling down
    private final Label processCountValue = new Label("05");
    private final Label resourceCountValue = new Label("03");
    private final Label systemStatusValue = new Label("—");
    private final Label safeSequenceValue = new Label("—");

    // these components show the step-by-step breakdown of how the safety algorithm reached its verdict
    private final VBox verificationLog = new VBox(4);
    private final Label statusBanner = new Label();
    private final Label availableDisplay = new Label("Initial Available [—]");
    private final Label systemMaxDisplay = new Label("System Max —");

    // shows error messages if someone enters negative numbers, letters, or allocation > max
    private final Label errorLabel = new Label();

    // main scrollable container stacking all our UI sections vertically with nice clean spacing
    private final VBox contentBox = new VBox(20);

    public BankerView() {
        contentBox.setPadding(new Insets(0));

        contentBox.getChildren().add(createHeader());
        contentBox.getChildren().add(createStatCards());
        contentBox.getChildren().add(createConfigRow());
        contentBox.getChildren().add(createMatricesRow());
        contentBox.getChildren().add(createBottomSection());
        contentBox.getChildren().add(createButtonRow());

        setContent(contentBox);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // initialize the 5x3 grids, fill in the sample problem from class, and run it right away so the UI isn't blank
        rebuildMatrices();
        loadDefaultData();
        runSafetyCheck();
    }

    // we hardcoded the classic Silberschatz textbook example (P0-P4 with resources A, B, C)
    // so the professor or TA testing this doesn't have to manually type 33 numbers every single run lol
    private void loadDefaultData() {
        int[][] defaultAlloc = {
            {0, 1, 0},
            {2, 0, 0},
            {3, 0, 2},
            {2, 1, 1},
            {0, 0, 2}
        };
        int[][] defaultMax = {
            {7, 5, 3},
            {3, 2, 2},
            {9, 0, 2},
            {2, 2, 2},
            {4, 3, 3}
        };
        int[] defaultAvail = {3, 3, 2};

        for (int i = 0; i < Math.min(numProcesses, defaultAlloc.length); i++) {
            for (int j = 0; j < Math.min(numResources, 3); j++) {
                allocationFields[i][j].setText(String.valueOf(defaultAlloc[i][j]));
                maxFields[i][j].setText(String.valueOf(defaultMax[i][j]));
            }
        }
        for (int j = 0; j < Math.min(numResources, 3); j++) {
            availableFields[j].setText(String.valueOf(defaultAvail[j]));
        }
    }

    // builds the top title bar with the module name and a "New Simulation" button to reset everything
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("Banker's Algorithm");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Label desc = new Label("Analyze deadlock avoidance and safe sequence calculations using resource allocation matrices.");
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

    // we put 4 quick summary cards here so you can immediately see process/resource counts, SAFE/UNSAFE, and the sequence
    private HBox createStatCards() {
        VBox card1 = buildStatCard("PROCESSES", processCountValue, "#22c55e", "Active process count");
        VBox card2 = buildStatCard("RESOURCE TYPES", resourceCountValue, "#3b82f6", "Vector dimension (A-C)");
        VBox card3 = buildStatCard("SYSTEM STATUS", systemStatusValue, "#22c55e", "Deadlock avoidance");
        VBox card4 = buildStatCard("SAFE SEQUENCE", safeSequenceValue, "#2dd4bf", "Valid dispatch order");

        systemStatusValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
        safeSequenceValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2dd4bf;");
        safeSequenceValue.setWrapText(true);

        HBox row = new HBox(14, card1, card2, card3, card4);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        return row;
    }

    // helper to make each stat card look uniform with a title, big value, status dot, and subtitle
    private VBox buildStatCard(String labelText, Label valueLabel, String accentColor, String descText) {
        Label cardLabel = new Label(labelText);
        cardLabel.getStyleClass().add("stat-card-label");

        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4; -fx-min-width: 8; -fx-min-height: 8; -fx-max-width: 8; -fx-max-height: 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(cardLabel, spacer, dot);
        topRow.setAlignment(Pos.CENTER);

        valueLabel.getStyleClass().add("stat-card-value");

        Label descLabel = new Label(descText);
        descLabel.getStyleClass().add("stat-card-description");
        descLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        VBox card = new VBox(4, topRow, valueLabel, descLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // this row lets the user pick how many processes and resources they want and click apply to resize the grids
    private HBox createConfigRow() {
        Label pLabel = new Label("Number of Processes:");
        pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        processSpinner.setPrefWidth(80);
        processSpinner.setEditable(true);

        Label rLabel = new Label("Number of Resource Types:");
        rLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        resourceSpinner.setPrefWidth(80);
        resourceSpinner.setEditable(true);

        Button applyBtn = new Button("Apply Configuration");
        applyBtn.getStyleClass().add("btn-outline");
        applyBtn.setOnAction(e -> {
            numProcesses = processSpinner.getValue();
            numResources = resourceSpinner.getValue();
            rebuildMatrices();
            processCountValue.setText(String.format("%02d", numProcesses));
            resourceCountValue.setText(String.format("%02d", numResources));
        });

        HBox row = new HBox(12, pLabel, processSpinner, rLabel, resourceSpinner, applyBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        return row;
    }

    // we placed all three matrices side-by-side in a horizontal row so you can easily compare Allocation vs Max vs Need
    private HBox createMatricesRow() {
        VBox allocPanel = wrapMatrixPanel("Allocation Matrix", allocationMatrix);
        VBox maxPanel = wrapMatrixPanel("Max Demand Matrix", maxMatrix);
        VBox needPanel = wrapMatrixPanel("Need Matrix (Max - Alloc)", needMatrix);

        HBox.setHgrow(allocPanel, Priority.ALWAYS);
        HBox.setHgrow(maxPanel, Priority.ALWAYS);
        HBox.setHgrow(needPanel, Priority.ALWAYS);

        HBox row = new HBox(14, allocPanel, maxPanel, needPanel);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    // wraps each matrix in a dark styled card container with a title up top
    private VBox wrapMatrixPanel(String title, VBox matrixContainer) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("panel-title");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox panel = new VBox(8, titleLabel, matrixContainer);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // completely redraws all the input grids whenever the number of processes or resources changes
    private void rebuildMatrices() {
        allocationFields = new TextField[numProcesses][numResources];
        maxFields = new TextField[numProcesses][numResources];
        needLabels = new Label[numProcesses][numResources];
        availableFields = new TextField[numResources];

        // generate input fields for the Allocation matrix (what processes currently hold)
        allocationMatrix.getChildren().clear();
        allocationMatrix.getChildren().add(createResourceHeaders());
        for (int i = 0; i < numProcesses; i++) {
            HBox row = createMatrixInputRow(i, allocationFields);
            allocationMatrix.getChildren().add(row);
        }

        // generate input fields for the Max Demand matrix (maximum each process can request)
        maxMatrix.getChildren().clear();
        maxMatrix.getChildren().add(createResourceHeaders());
        for (int i = 0; i < numProcesses; i++) {
            HBox row = createMatrixInputRow(i, maxFields);
            maxMatrix.getChildren().add(row);
        }

        // generate labels for the Need matrix — these are read-only because Need = Max - Allocation
        needMatrix.getChildren().clear();
        needMatrix.getChildren().add(createResourceHeaders());
        for (int i = 0; i < numProcesses; i++) {
            HBox row = createNeedRow(i);
            needMatrix.getChildren().add(row);
        }

        // input fields for the Available vector (how many free instances of each resource currently exist)
        availableRow.getChildren().clear();
        for (int j = 0; j < numResources; j++) {
            Label resLabel = new Label(resourceName(j) + ":");
            resLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
            TextField field = new TextField();
            field.setPrefWidth(50);
            field.setPromptText("0");
            availableFields[j] = field;
            availableRow.getChildren().addAll(resLabel, field);
        }

        // we added listeners to every single cell so the Need matrix recalculates on the fly as you type!
        for (int i = 0; i < numProcesses; i++) {
            final int pi = i;
            for (int j = 0; j < numResources; j++) {
                final int pj = j;
                allocationFields[i][j].textProperty().addListener((obs, o, n) -> updateNeedCell(pi, pj));
                maxFields[i][j].textProperty().addListener((obs, o, n) -> updateNeedCell(pi, pj));
            }
        }

        // clear out any old run results so stale data doesn't confuse anyone
        systemStatusValue.setText("—");
        systemStatusValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        safeSequenceValue.setText("—");
        safeSequenceValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        verificationLog.getChildren().clear();
        statusBanner.setText("");
        availableDisplay.setText("Initial Available [—]");
        systemMaxDisplay.setText("System Max —");
        errorLabel.setText("");
    }

    // draws resource column headers (A, B, C...) with a blank spacer so they align over the input columns
    private HBox createResourceHeaders() {
        Label spacer = new Label("");
        spacer.setPrefWidth(36);

        HBox row = new HBox(4, spacer);
        row.setAlignment(Pos.CENTER_LEFT);
        for (int j = 0; j < numResources; j++) {
            Label header = new Label(resourceName(j));
            header.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold; " +
                    "-fx-min-width: 50; -fx-alignment: center;");
            header.setPrefWidth(54);
            header.setAlignment(Pos.CENTER);
            row.getChildren().add(header);
        }
        return row;
    }

    // builds one row of text inputs for process Pi with a label on the left
    private HBox createMatrixInputRow(int processIndex, TextField[][] targetArray) {
        Label pLabel = new Label("P" + processIndex);
        pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
        pLabel.setPrefWidth(36);

        HBox row = new HBox(4, pLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        for (int j = 0; j < numResources; j++) {
            TextField field = new TextField();
            field.setPrefWidth(50);
            field.setAlignment(Pos.CENTER);
            field.setStyle("-fx-font-size: 12px;");
            field.setPromptText("0");
            targetArray[processIndex][j] = field;
            row.getChildren().add(field);
        }
        return row;
    }

    // builds one row of non-editable labels for the Need matrix
    private HBox createNeedRow(int processIndex) {
        Label pLabel = new Label("P" + processIndex);
        pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
        pLabel.setPrefWidth(36);

        HBox row = new HBox(4, pLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        for (int j = 0; j < numResources; j++) {
            Label label = new Label("");
            label.setPrefWidth(50);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; " +
                    "-fx-background-color: #151d2b; -fx-background-radius: 4; " +
                    "-fx-padding: 6 8 6 8; -fx-min-height: 28;");
            needLabels[processIndex][j] = label;
            row.getChildren().add(label);
        }
        return row;
    }

    // calculates Need = Max - Allocation for cell [i][j]; we highlight it red if Need < 0 because that violates OS rules!
    private void updateNeedCell(int i, int j) {
        try {
            int alloc = parseIntSafe(allocationFields[i][j].getText());
            int max = parseIntSafe(maxFields[i][j].getText());
            int need = max - alloc;
            needLabels[i][j].setText(String.valueOf(need));
            if (need < 0) {
                needLabels[i][j].setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; " +
                        "-fx-background-color: #151d2b; -fx-background-radius: 4; " +
                        "-fx-padding: 6 8 6 8; -fx-min-height: 28;");
            } else {
                needLabels[i][j].setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; " +
                        "-fx-background-color: #151d2b; -fx-background-radius: 4; " +
                        "-fx-padding: 6 8 6 8; -fx-min-height: 28;");
            }
        } catch (NumberFormatException e) {
            needLabels[i][j].setText("?");
        }
    }

    // bottom panel: Available resource vector on the left, step-by-step verification log on the right
    private VBox createBottomSection() {
        Label sectionTitle = new Label("Available Vector & Safety Verification Log");
        sectionTitle.getStyleClass().add("panel-title");

        // left column: inputs for available resources plus calculated system totals
        Label avLabel = new Label("Resource Available Vector");
        avLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        availableDisplay.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 4 0 4 0;");
        systemMaxDisplay.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        VBox leftSection = new VBox(6, avLabel, availableRow, availableDisplay, systemMaxDisplay);
        leftSection.setMinWidth(280);
        leftSection.setPrefWidth(320);

        // right column: scrollable log showing exactly how the safety algorithm tests each process
        Label logLabel = new Label("Execution Verification Log");
        logLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        verificationLog.setPadding(new Insets(4));
        ScrollPane logScroll = new ScrollPane(verificationLog);
        logScroll.setPrefHeight(180);
        logScroll.setFitToWidth(true);
        logScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        statusBanner.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: bold; " +
                "-fx-padding: 8; -fx-background-color: derive(#22c55e, -85%); -fx-background-radius: 6;");
        statusBanner.setWrapText(true);
        statusBanner.setMaxWidth(Double.MAX_VALUE);

        VBox rightSection = new VBox(6, logLabel, logScroll, statusBanner);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        HBox content = new HBox(20, leftSection, rightSection);

        // red warning banner at the bottom for any input validation errors
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        VBox panel = new VBox(10, sectionTitle, content, errorLabel);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // action buttons to either evaluate the matrices or reset back to default
    private HBox createButtonRow() {
        Button runBtn = new Button("Run Safety Check");
        runBtn.getStyleClass().add("btn-accent");
        runBtn.setOnAction(e -> runSafetyCheck());

        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add("btn-outline");
        resetBtn.setOnAction(e -> resetAll());

        HBox row = new HBox(12, runBtn, resetBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /* this is the main action: we pull all numbers from the UI, validate everything,
       and pass them to BankerEngine to find a safe sequence */
    private void runSafetyCheck() {
        errorLabel.setText("");
        verificationLog.getChildren().clear();

        // step 1: read all values from the text fields and convert them into 2D int arrays
        int[][] allocation = new int[numProcesses][numResources];
        int[][] maximum = new int[numProcesses][numResources];
        int[] available = new int[numResources];

        try {
            for (int i = 0; i < numProcesses; i++) {
                for (int j = 0; j < numResources; j++) {
                    allocation[i][j] = parseAndValidate(allocationFields[i][j],
                            "Allocation[P" + i + "][" + resourceName(j) + "]");
                    maximum[i][j] = parseAndValidate(maxFields[i][j],
                            "Max[P" + i + "][" + resourceName(j) + "]");
                }
            }
            for (int j = 0; j < numResources; j++) {
                available[j] = parseAndValidate(availableFields[j],
                        "Available[" + resourceName(j) + "]");
            }
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
            return;
        }

        // step 2: make sure basic OS rules aren't broken (e.g. Allocation can't be greater than Max)
        List<String> errors = engine.validate(allocation, maximum, available);
        if (!errors.isEmpty()) {
            errorLabel.setText(String.join("\n", errors));
            return;
        }

        // step 3: run the safety check algorithm using our BankerState model and BankerEngine
        session.setSimulationState(SimulationState.RUNNING);
        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        // step 4: update the stat cards so the user sees SAFE/UNSAFE and the sequence right away
        processCountValue.setText(String.format("%02d", numProcesses));
        resourceCountValue.setText(String.format("%02d", numResources));

        if (result.isSafe()) {
            systemStatusValue.setText("SAFE");
            systemStatusValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");

            String seqStr = String.join(" → ", result.getSafeSequence());
            safeSequenceValue.setText(seqStr);
            safeSequenceValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2dd4bf;");
        } else {
            systemStatusValue.setText("UNSAFE");
            systemStatusValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

            safeSequenceValue.setText("No safe sequence exists");
            safeSequenceValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        }

        // step 5: format and display the initial available vector in the log panel
        StringBuilder avStr = new StringBuilder("Initial Available [");
        for (int j = 0; j < numResources; j++) {
            if (j > 0) avStr.append(" | ");
            avStr.append(resourceName(j)).append(": ").append(available[j]);
        }
        avStr.append("]");
        availableDisplay.setText(avStr.toString());

        // step 6: show total resources in the entire system (Available + sum of all Allocations)
        int[] totalResources = state.getTotalResources();
        StringBuilder maxStr = new StringBuilder("System Max ");
        for (int j = 0; j < numResources; j++) {
            if (j > 0) maxStr.append(" | ");
            maxStr.append(resourceName(j)).append(": ").append(totalResources[j]);
        }
        systemMaxDisplay.setText(maxStr.toString());

        // step 7: sync the calculated Need values back into the Need matrix labels
        int[][] need = state.getNeed();
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                needLabels[i][j].setText(String.valueOf(need[i][j]));
            }
        }

        // step 8: write out each step in the log so students can trace along with how work vector updates
        for (SafetyResult.SafetyStep step : result.getSteps()) {
            Label stepLabel = new Label(step.getDescription());
            stepLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; " +
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;");
            stepLabel.setWrapText(true);
            stepLabel.setMaxWidth(Double.MAX_VALUE);
            verificationLog.getChildren().add(stepLabel);
        }

        // step 9: show a clear colored banner (green if safe, red if unsafe with deadlocked processes)
        if (result.isSafe()) {
            statusBanner.setText("Safety check complete. System is in a SAFE STATE.");
            statusBanner.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#22c55e, -85%); -fx-background-radius: 6;");
        } else {
            String unfinished = String.join(", ", result.getUnfinishedProcesses());
            statusBanner.setText("System is UNSAFE. Deadlock possible. Unfinished: " + unfinished);
            statusBanner.setStyle("-fx-font-size: 13px; -fx-text-fill: #ef4444; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#ef4444, -85%); -fx-background-radius: 6;");
        }
    }

    // wipes all fields, rebuilds the matrices based on current spinner counts, and resets state
    private void resetAll() {
        numProcesses = processSpinner.getValue();
        numResources = resourceSpinner.getValue();
        rebuildMatrices();
        processCountValue.setText(String.format("%02d", numProcesses));
        resourceCountValue.setText(String.format("%02d", numResources));
        session.setSimulationState(SimulationState.IDLE);
    }

    // helper methods for parsing text inputs and converting column numbers to resource letters
    // helper to turn resource index 0 into 'A', 1 into 'B', etc. by casting chars
    private static String resourceName(int index) {
        return String.valueOf((char) ('A' + index));
    }

    // helper to parse integer inputs — blanks default to 0, and negatives throw an exception
    private int parseAndValidate(TextField field, String name) throws ValidationException {
        String text = field.getText().trim();
        if (text.isEmpty()) return 0;
        try {
            int val = Integer.parseInt(text);
            if (val < 0) {
                throw new ValidationException(name + " must be non-negative. Got: " + val);
            }
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be a valid integer. Got: '" + text + "'");
        }
    }

    // safely parse numbers for the live Need calculation without crashing on empty inputs
    private int parseIntSafe(String text) {
        if (text == null || text.isBlank()) return 0;
        return Integer.parseInt(text.trim());
    }

    // custom exception we throw when someone types invalid text so we can catch it and show an error banner
    private static class ValidationException extends Exception {
        ValidationException(String message) { super(message); }
    }
}
