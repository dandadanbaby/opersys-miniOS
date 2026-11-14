package com.beastos.pagereplacement.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.pagereplacement.engine.*;
import com.beastos.pagereplacement.model.PageReplacementOutput;
import com.beastos.pagereplacement.model.PageReplacementStep;
import com.beastos.validation.InputValidator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Page Replacement module view for BEAST OS.
 * Matches design: frame state evolution grid, step-by-step table,
 * algorithm comparison tracker, and hit/fault statistics.
 */
public class PageReplacementView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();

    // Algorithm selection
    private String selectedAlgorithm = "FIFO";
    private final List<Button> algoTabs = new ArrayList<>();

    // Input controls
    private final TextField refStringField = new TextField();
    private final Spinner<Integer> frameCountSpinner = new Spinner<>(1, 20, 3);

    // Stat cards
    private final Label totalReqValue = new Label("00");
    private final Label hitsValue = new Label("00");
    private final Label faultsValue = new Label("00");
    private final Label hitRatioValue = new Label("0.0%");

    // Frame evolution grid
    private final VBox frameEvolutionGrid = new VBox(0);

    // Step-by-step table
    private final TableView<PageReplacementStep> stepTable = new TableView<>();

    // Algorithm comparison
    private final VBox comparisonTracker = new VBox(8);

    // Error label
    private final Label errorLabel = new Label();

    public PageReplacementView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        content.getChildren().add(createHeader());
        content.getChildren().add(createStatCards());
        content.getChildren().add(createInputRow());
        content.getChildren().add(createFrameEvolutionPanel());
        content.getChildren().add(createBottomSection());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Pre-run default simulation matching page-replacement.png
        runSimulation();
    }

    // ----------------------------------------------------------------
    //  Header
    // ----------------------------------------------------------------
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");
        Label title = new Label("Page Replacement");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");
        Label desc = new Label("Analyze memory page evolution, hits, and faults over a page reference string.");
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
        VBox c1 = buildStatCard("TOTAL REQUESTS", totalReqValue, "+15 this string", "#3b82f6");
        VBox c2 = buildStatCard("PAGE HITS", hitsValue, "Frames retained", "#22c55e");
        hitsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
        VBox c3 = buildStatCard("PAGE FAULTS", faultsValue, "Miss penalty count", "#ef4444");
        faultsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        VBox c4 = buildStatCard("HIT RATIO", hitRatioValue, "Efficiency rate", "#2dd4bf");

        HBox row = new HBox(14, c1, c2, c3, c4);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);
        return row;
    }

    private VBox buildStatCard(String labelText, Label valueLabel, String descText, String accentColor) {
        Label cardLabel = new Label(labelText);
        cardLabel.getStyleClass().add("stat-card-label");
        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4; -fx-min-width: 8; -fx-min-height: 8; -fx-max-width: 8; -fx-max-height: 8;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(cardLabel, sp, dot);
        topRow.setAlignment(Pos.CENTER);
        valueLabel.getStyleClass().add("stat-card-value");
        Label descLabel = new Label(descText);
        descLabel.getStyleClass().add("stat-card-description");
        descLabel.setStyle("-fx-text-fill: " + accentColor + ";");
        VBox card = new VBox(4, topRow, valueLabel, descLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ----------------------------------------------------------------
    //  Input Row
    // ----------------------------------------------------------------
    private VBox createInputRow() {
        // Algorithm tabs
        HBox tabBar = createAlgorithmTabs();

        // Reference string input
        Label refLabel = new Label("Page Reference String (comma or space separated):");
        refLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        refStringField.setPromptText("7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1");
        refStringField.setPrefWidth(500);

        Label frameLabel = new Label("Frame Count:");
        frameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        frameCountSpinner.setPrefWidth(70);
        frameCountSpinner.setEditable(true);

        Button runBtn = new Button("Run Simulation");
        runBtn.getStyleClass().add("btn-accent");
        runBtn.setOnAction(e -> runSimulation());

        Button runAllBtn = new Button("Compare All");
        runAllBtn.getStyleClass().add("btn-outline");
        runAllBtn.setOnAction(e -> runAllAlgorithms());

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        HBox inputRow = new HBox(12, refLabel, refStringField, frameLabel, frameCountSpinner, runBtn, runAllBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        // Wrap for narrow windows
        VBox inputPanel = new VBox(8, tabBar, inputRow, errorLabel);
        inputPanel.getStyleClass().add("panel");
        return inputPanel;
    }

    private HBox createAlgorithmTabs() {
        String[] algorithms = {"FIFO", "LRU", "Optimal"};
        HBox tabBar = new HBox(8);
        tabBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Frame State Evolution");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");

        Label paren = new Label("(");
        paren.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");

        for (int i = 0; i < algorithms.length; i++) {
            String algo = algorithms[i];
            Button tab = new Button(algo);
            tab.getStyleClass().add("algo-tab");
            if (algo.equals(selectedAlgorithm)) tab.getStyleClass().add("algo-tab-active");
            tab.setOnAction(e -> selectAlgorithm(algo));
            algoTabs.add(tab);
            tabBar.getChildren().add(tab);
            if (i < algorithms.length - 1) {
                Label sep = new Label("|");
                sep.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
                tabBar.getChildren().add(sep);
            }
        }

        return tabBar;
    }

    private void selectAlgorithm(String algo) {
        selectedAlgorithm = algo;
        for (Button tab : algoTabs) {
            tab.getStyleClass().remove("algo-tab-active");
            if (tab.getText().equals(algo)) tab.getStyleClass().add("algo-tab-active");
        }
    }

    // ----------------------------------------------------------------
    //  Frame Evolution Grid (colored grid like design)
    // ----------------------------------------------------------------
    private VBox createFrameEvolutionPanel() {
        Label subtitle = new Label("Column Page Requests (Ref String)");
        subtitle.getStyleClass().add("panel-subtitle");

        ScrollPane gridScroll = new ScrollPane(frameEvolutionGrid);
        gridScroll.setFitToHeight(true);
        gridScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        gridScroll.setPrefHeight(220);

        VBox panel = new VBox(8, subtitle, gridScroll);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void buildFrameEvolutionGrid(PageReplacementOutput output) {
        frameEvolutionGrid.getChildren().clear();

        int[] refString = output.getReferenceString();
        int frames = output.getFrameCount();
        List<PageReplacementStep> steps = output.getSteps();

        // Row 0: PAGE reference row
        HBox pageRow = new HBox(0);
        Label pageLabel = new Label("PAGE");
        pageLabel.setStyle("-fx-min-width: 80; -fx-font-size: 11px; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 6 8;");
        pageRow.getChildren().add(pageLabel);

        for (int i = 0; i < refString.length; i++) {
            Label cell = new Label(String.valueOf(refString[i]));
            cell.setStyle("-fx-min-width: 48; -fx-pref-width: 48; -fx-alignment: center; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc; " +
                    "-fx-background-color: #1e4a5a; -fx-background-radius: 4; " +
                    "-fx-padding: 6; -fx-border-color: #0d1117; -fx-border-width: 1;");
            pageRow.getChildren().add(cell);
        }
        frameEvolutionGrid.getChildren().add(pageRow);

        // Frame rows
        for (int f = 0; f < frames; f++) {
            HBox row = new HBox(0);
            Label fLabel = new Label("FRAME " + f);
            fLabel.setStyle("-fx-min-width: 80; -fx-font-size: 11px; -fx-text-fill: #64748b; " +
                    "-fx-font-weight: bold; -fx-padding: 6 8;");
            row.getChildren().add(fLabel);

            for (int i = 0; i < steps.size(); i++) {
                PageReplacementStep step = steps.get(i);
                int[] state = step.getFrameState();
                String val = (f < state.length && state[f] >= 0) ? String.valueOf(state[f]) : "-";
                Label cell = new Label(val);
                cell.setStyle("-fx-min-width: 48; -fx-pref-width: 48; -fx-alignment: center; " +
                        "-fx-font-size: 13px; -fx-text-fill: #f8fafc; " +
                        "-fx-background-color: #1c2333; -fx-background-radius: 2; " +
                        "-fx-padding: 6; -fx-border-color: #0d1117; -fx-border-width: 1;");
                row.getChildren().add(cell);
            }
            frameEvolutionGrid.getChildren().add(row);
        }

        // Status row
        HBox statusRow = new HBox(0);
        Label sLabel = new Label("STATUS");
        sLabel.setStyle("-fx-min-width: 80; -fx-font-size: 11px; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 6 8;");
        statusRow.getChildren().add(sLabel);

        for (PageReplacementStep step : steps) {
            Label cell = new Label(step.getStatusText());
            String bgColor = step.isHit() ? "#14716a" : "#7c2d12";
            cell.setStyle("-fx-min-width: 48; -fx-pref-width: 48; -fx-alignment: center; " +
                    "-fx-font-size: 10px; -fx-font-weight: bold; " +
                    "-fx-text-fill: " + (step.isHit() ? "#2dd4bf" : "#ef4444") + "; " +
                    "-fx-background-color: " + bgColor + "; -fx-background-radius: 4; " +
                    "-fx-padding: 6; -fx-border-color: #0d1117; -fx-border-width: 1;");
            statusRow.getChildren().add(cell);
        }
        frameEvolutionGrid.getChildren().add(statusRow);
    }

    // ----------------------------------------------------------------
    //  Bottom Section — Step Table + Comparison
    // ----------------------------------------------------------------
    private HBox createBottomSection() {
        // Left: step table
        Label tableTitle = new Label("Page Replacement Step-by-Step Table & Algorithm Comparison");
        tableTitle.getStyleClass().add("panel-title");
        tableTitle.setStyle("-fx-font-size: 16px;");

        Label tableSubtitle = new Label("Step-by-Step State Transition Table");
        tableSubtitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        setupStepTable();

        VBox leftSection = new VBox(8, tableTitle, tableSubtitle, stepTable);
        HBox.setHgrow(leftSection, Priority.ALWAYS);

        // Right: comparison tracker
        Label compTitle = new Label("Algorithm Comparison Tracker");
        compTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox rightSection = new VBox(8, compTitle, comparisonTracker);
        rightSection.setMinWidth(280);
        rightSection.setPrefWidth(320);

        HBox bottom = new HBox(20, leftSection, rightSection);
        bottom.getStyleClass().add("panel");
        return bottom;
    }

    @SuppressWarnings("unchecked")
    private void setupStepTable() {
        TableColumn<PageReplacementStep, String> pageCol = new TableColumn<>("Page");
        pageCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getPage())));
        pageCol.setPrefWidth(55);

        TableColumn<PageReplacementStep, String> frameCol = new TableColumn<>("Frames State");
        frameCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getFrameStateString()));
        frameCol.setPrefWidth(160);

        TableColumn<PageReplacementStep, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatusText()));
        statusCol.setPrefWidth(70);

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    if ("HIT".equals(item)) setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });

        stepTable.getColumns().addAll(pageCol, frameCol, statusCol);
        stepTable.setPlaceholder(new Label("Run a simulation to see step-by-step results"));
        stepTable.setPrefHeight(250);
        stepTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    // ----------------------------------------------------------------
    //  Simulation Execution
    // ----------------------------------------------------------------
    private int[] parseRefString() {
        String text = refStringField.getText().trim();
        if (text.isEmpty()) {
            // Use default reference string from design
            text = "7,0,1,2,0,3,0,4,2,3,0,3,2,1,2,0,1,7,0,1";
            refStringField.setText(text);
        }
        int[] arr = InputValidator.parseIntArray(text);
        if (arr == null) return null;
        for (int v : arr) {
            if (v < 0) return null;
        }
        return arr;
    }

    private void runSimulation() {
        errorLabel.setText("");

        int[] refString = parseRefString();
        if (refString == null) {
            errorLabel.setText("Invalid reference string. Use comma or space-separated non-negative integers.");
            return;
        }

        int frameCount = frameCountSpinner.getValue();

        PageReplacementAlgorithm algorithm = switch (selectedAlgorithm) {
            case "FIFO" -> new FifoReplacement();
            case "LRU" -> new LruReplacement();
            case "Optimal" -> new OptimalReplacement();
            default -> new FifoReplacement();
        };

        session.setSimulationState(SimulationState.RUNNING);
        PageReplacementOutput output = algorithm.simulate(refString, frameCount);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        updateDisplay(output);
    }

    private void runAllAlgorithms() {
        errorLabel.setText("");

        int[] refString = parseRefString();
        if (refString == null) {
            errorLabel.setText("Invalid reference string. Use comma or space-separated non-negative integers.");
            return;
        }

        int frameCount = frameCountSpinner.getValue();

        PageReplacementAlgorithm[] algorithms = {
                new FifoReplacement(), new LruReplacement(), new OptimalReplacement()
        };

        session.setSimulationState(SimulationState.RUNNING);

        PageReplacementOutput selectedOutput = null;
        comparisonTracker.getChildren().clear();

        for (PageReplacementAlgorithm algo : algorithms) {
            PageReplacementOutput output = algo.simulate(refString, frameCount);

            if (algo.getAlgorithmName().equals(selectedAlgorithm)) {
                selectedOutput = output;
            }

            // Add comparison card
            comparisonTracker.getChildren().add(createComparisonCard(output));
        }

        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        if (selectedOutput != null) {
            updateDisplay(selectedOutput);
        }
    }

    private void updateDisplay(PageReplacementOutput output) {
        totalReqValue.setText(String.format("%02d", output.getTotalRequests()));
        hitsValue.setText(String.format("%02d", output.getHitCount()));
        hitsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
        faultsValue.setText(String.format("%02d", output.getFaultCount()));
        faultsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        hitRatioValue.setText(String.format("%.1f%%", output.getHitRatio()));

        buildFrameEvolutionGrid(output);
        stepTable.setItems(FXCollections.observableArrayList(output.getSteps()));
    }

    private VBox createComparisonCard(PageReplacementOutput output) {
        String algoName = output.getAlgorithmName();
        String fullName = switch (algoName) {
            case "FIFO" -> "FIFO (First-In, First-Out)";
            case "LRU" -> "LRU (Least Recently Used)";
            case "Optimal" -> "Optimal Page Replacement";
            default -> algoName;
        };

        Label nameLabel = new Label(fullName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label ratioLabel = new Label(String.format("%.1f%% Hit Ratio", output.getHitRatio()));
        ratioLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(8, nameLabel, sp, ratioLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Progress bar
        ProgressBar bar = new ProgressBar(output.getHitRatio() / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: #2dd4bf;");

        VBox card = new VBox(4, topRow, bar);
        card.setStyle("-fx-background-color: #1c2333; -fx-background-radius: 6; " +
                "-fx-border-color: #334155; -fx-border-radius: 6; -fx-padding: 10;");
        return card;
    }

    private void resetAll() {
        refStringField.clear();
        totalReqValue.setText("00");
        hitsValue.setText("00");
        hitsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
        faultsValue.setText("00");
        faultsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        hitRatioValue.setText("0.0%");
        frameEvolutionGrid.getChildren().clear();
        stepTable.getItems().clear();
        comparisonTracker.getChildren().clear();
        errorLabel.setText("");
        session.setSimulationState(SimulationState.IDLE);
    }
}
