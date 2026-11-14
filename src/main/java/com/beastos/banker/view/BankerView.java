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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.List;

/**
 * Banker's Algorithm module view for BEAST OS.
 * Matches the supplied design reference: stat cards, three matrices
 * (Allocation, Max Demand, Need), available vector, and verification log.
 */
public class BankerView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final BankerEngine engine = new BankerEngine();

    // Configuration
    private int numProcesses = 5;
    private int numResources = 3;

    // Config controls
    private final Spinner<Integer> processSpinner = new Spinner<>(1, 50, 5);
    private final Spinner<Integer> resourceSpinner = new Spinner<>(1, 26, 3); // max 26 for A-Z

    // Matrices — 2D arrays of TextFields
    private TextField[][] allocationFields;
    private TextField[][] maxFields;
    private Label[][] needLabels;
    private TextField[] availableFields;

    // Matrix containers (rebuilt on config change)
    private final VBox allocationMatrix = new VBox(4);
    private final VBox maxMatrix = new VBox(4);
    private final VBox needMatrix = new VBox(4);
    private final HBox availableRow = new HBox(8);

    // Stat card labels
    private final Label processCountValue = new Label("05");
    private final Label resourceCountValue = new Label("03");
    private final Label systemStatusValue = new Label("—");
    private final Label safeSequenceValue = new Label("—");

    // Verification log
    private final VBox verificationLog = new VBox(4);
    private final Label statusBanner = new Label();
    private final Label availableDisplay = new Label("Initial Available [—]");
    private final Label systemMaxDisplay = new Label("System Max —");

    // Error label
    private final Label errorLabel = new Label();

    // Main content container
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

        // Build initial matrices and run safety check
        rebuildMatrices();
        runSafetyCheck();
    }

    // ----------------------------------------------------------------
    //  Header
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    //  Stat Cards
    // ----------------------------------------------------------------
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

        VBox card = new VBox(4, topRow, valueLabel, descLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ----------------------------------------------------------------
    //  Configuration Row
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    //  Three Matrices Side by Side
    // ----------------------------------------------------------------
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

    private VBox wrapMatrixPanel(String title, VBox matrixContainer) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("panel-title");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox panel = new VBox(8, titleLabel, matrixContainer);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void rebuildMatrices() {
        allocationFields = new TextField[numProcesses][numResources];
        maxFields = new TextField[numProcesses][numResources];
        needLabels = new Label[numProcesses][numResources];
        availableFields = new TextField[numResources];

        // Rebuild Allocation Matrix
        allocationMatrix.getChildren().clear();
        allocationMatrix.getChildren().add(createResourceHeaders());
        for (int i = 0; i < numProcesses; i++) {
            HBox row = createMatrixInputRow(i, allocationFields);
            allocationMatrix.getChildren().add(row);
        }

        // Rebuild Max Matrix
        maxMatrix.getChildren().clear();
        maxMatrix.getChildren().add(createResourceHeaders());
        for (int i = 0; i < numProcesses; i++) {
            HBox row = createMatrixInputRow(i, maxFields);
            maxMatrix.getChildren().add(row);
        }

        // Rebuild Need Matrix (read-only)
        needMatrix.getChildren().clear();
        needMatrix.getChildren().add(createResourceHeaders());
        for (int i = 0; i < numProcesses; i++) {
            HBox row = createNeedRow(i);
            needMatrix.getChildren().add(row);
        }

        // Rebuild Available vector
        availableRow.getChildren().clear();
        for (int j = 0; j < numResources; j++) {
            Label resLabel = new Label(resourceName(j) + ":");
            resLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
            TextField field = new TextField("0");
            field.setPrefWidth(50);
            availableFields[j] = field;
            availableRow.getChildren().addAll(resLabel, field);
        }

        // Add listeners to update Need on input change
        for (int i = 0; i < numProcesses; i++) {
            final int pi = i;
            for (int j = 0; j < numResources; j++) {
                final int pj = j;
                allocationFields[i][j].textProperty().addListener((obs, o, n) -> updateNeedCell(pi, pj));
                maxFields[i][j].textProperty().addListener((obs, o, n) -> updateNeedCell(pi, pj));
            }
        }

        // Reset displays
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

    private HBox createMatrixInputRow(int processIndex, TextField[][] targetArray) {
        Label pLabel = new Label("P" + processIndex);
        pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
        pLabel.setPrefWidth(36);

        HBox row = new HBox(4, pLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        for (int j = 0; j < numResources; j++) {
            TextField field = new TextField("0");
            field.setPrefWidth(50);
            field.setAlignment(Pos.CENTER);
            field.setStyle("-fx-font-size: 12px;");
            targetArray[processIndex][j] = field;
            row.getChildren().add(field);
        }
        return row;
    }

    private HBox createNeedRow(int processIndex) {
        Label pLabel = new Label("P" + processIndex);
        pLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
        pLabel.setPrefWidth(36);

        HBox row = new HBox(4, pLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        for (int j = 0; j < numResources; j++) {
            Label label = new Label("0");
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

    // ----------------------------------------------------------------
    //  Bottom Section — Available Vector + Verification Log
    // ----------------------------------------------------------------
    private VBox createBottomSection() {
        Label sectionTitle = new Label("Available Vector & Safety Verification Log");
        sectionTitle.getStyleClass().add("panel-title");

        // Left: Available vector input + info
        Label avLabel = new Label("Resource Available Vector");
        avLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        availableDisplay.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 4 0 4 0;");
        systemMaxDisplay.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        VBox leftSection = new VBox(6, avLabel, availableRow, availableDisplay, systemMaxDisplay);
        leftSection.setMinWidth(280);
        leftSection.setPrefWidth(320);

        // Right: Verification log
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

        // Error label
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        VBox panel = new VBox(10, sectionTitle, content, errorLabel);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // ----------------------------------------------------------------
    //  Button Row
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    //  Safety Check Execution
    // ----------------------------------------------------------------
    private void runSafetyCheck() {
        errorLabel.setText("");
        verificationLog.getChildren().clear();

        // Parse matrices
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

        // Validate using engine
        List<String> errors = engine.validate(allocation, maximum, available);
        if (!errors.isEmpty()) {
            errorLabel.setText(String.join("\n", errors));
            return;
        }

        // Run safety check
        session.setSimulationState(SimulationState.RUNNING);
        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        // Update stat cards
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

        // Update available display
        StringBuilder avStr = new StringBuilder("Initial Available [");
        for (int j = 0; j < numResources; j++) {
            if (j > 0) avStr.append(" | ");
            avStr.append(resourceName(j)).append(": ").append(available[j]);
        }
        avStr.append("]");
        availableDisplay.setText(avStr.toString());

        // System max
        int[] totalResources = state.getTotalResources();
        StringBuilder maxStr = new StringBuilder("System Max ");
        for (int j = 0; j < numResources; j++) {
            if (j > 0) maxStr.append(" | ");
            maxStr.append(resourceName(j)).append(": ").append(totalResources[j]);
        }
        systemMaxDisplay.setText(maxStr.toString());

        // Update Need matrix display
        int[][] need = state.getNeed();
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                needLabels[i][j].setText(String.valueOf(need[i][j]));
            }
        }

        // Build verification log
        for (SafetyResult.SafetyStep step : result.getSteps()) {
            Label stepLabel = new Label(step.getDescription());
            stepLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; " +
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;");
            stepLabel.setWrapText(true);
            stepLabel.setMaxWidth(Double.MAX_VALUE);
            verificationLog.getChildren().add(stepLabel);
        }

        // Status banner
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

    private void resetAll() {
        numProcesses = processSpinner.getValue();
        numResources = resourceSpinner.getValue();
        rebuildMatrices();
        processCountValue.setText(String.format("%02d", numProcesses));
        resourceCountValue.setText(String.format("%02d", numResources));
        session.setSimulationState(SimulationState.IDLE);
    }

    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------
    private static String resourceName(int index) {
        return String.valueOf((char) ('A' + index));
    }

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

    private int parseIntSafe(String text) {
        if (text == null || text.isBlank()) return 0;
        return Integer.parseInt(text.trim());
    }

    private static class ValidationException extends Exception {
        ValidationException(String message) { super(message); }
    }
}
