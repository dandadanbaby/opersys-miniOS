package com.beastos.paging.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.paging.engine.PagingEngine;
import com.beastos.paging.model.PagingOutput;
import com.beastos.paging.model.TranslationResult;
import com.beastos.paging.model.TwoLevelPagingOutput;
import com.beastos.paging.model.TwoLevelTranslationResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Paging module view for BEAST OS.
 * Supports both Single-Level Paging and Two-Level (Hierarchical / Multi-Level) Paging.
 * Matches design: page map table, logical-to-physical translation flowchart,
 * and address translation verification log.
 */
public class PagingView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final PagingEngine engine = new PagingEngine();

    // Mode selection: "SINGLE" vs "TWO_LEVEL"
    private String pagingMode = "SINGLE";
    private Button singleLevelTab;
    private Button twoLevelTab;
    private final VBox singleLevelMainArea;
    private final VBox twoLevelMainArea;

    // Stat cards
    private final Label pageSizeValue = new Label("4096 B");
    private final Label logicalPagesValue = new Label("08");
    private final Label mappedFrameValue = new Label("—");
    private final Label physicalAddrValue = new Label("—");
    private final Label logicalPagesCardTitle = new Label("LOGICAL PAGES");

    // ==========================================
    // Single-Level Paging Controls
    // ==========================================
    private final TextField pageSizeField = new TextField("4096");
    private final Spinner<Integer> pageCountSpinner = new Spinner<>(1, 100, 8);
    private final VBox pageTableRows = new VBox(4);
    private TextField[] frameFields;
    private final VBox addressRows = new VBox(4);
    private final VBox translationFlowchart = new VBox(10);
    private final TableView<TranslationResult> verificationTable = new TableView<>();
    private final Label verificationStatus = new Label();
    private final Label verificationCount = new Label();
    private final Label errorLabel = new Label();

    // ==========================================
    // Two-Level (Multi-Level) Paging Controls
    // ==========================================
    private final TextField twoLevelPageSizeField = new TextField("1024");
    private final Spinner<Integer> outerCountSpinner = new Spinner<>(1, 8, 4);
    private final Spinner<Integer> innerCountSpinner = new Spinner<>(1, 8, 4);
    private final VBox twoLevelDirectoryRows = new VBox(8);
    private TextField[][] twoLevelFrameFields; // [outer][inner]
    private final VBox twoLevelAddressRows = new VBox(4);
    private final VBox twoLevelFlowchart = new VBox(10);
    private final TableView<TwoLevelTranslationResult> twoLevelVerificationTable = new TableView<>();
    private final Label twoLevelVerificationStatus = new Label();
    private final Label twoLevelVerificationCount = new Label();
    private final Label twoLevelErrorLabel = new Label();

    public PagingView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(0));

        content.getChildren().add(createHeader());
        content.getChildren().add(createModeTabBar());
        content.getChildren().add(createStatCards());

        singleLevelMainArea = createSingleLevelMainArea();
        twoLevelMainArea = createTwoLevelMainArea();
        twoLevelMainArea.setVisible(false);
        twoLevelMainArea.setManaged(false);

        content.getChildren().addAll(singleLevelMainArea, twoLevelMainArea);

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Setup Single-Level defaults
        rebuildPageTable();
        int[] defaultFrames = {5, 2, 9, 3, 12, 15, 0, 7};
        for (int i = 0; i < defaultFrames.length && i < frameFields.length; i++) {
            frameFields[i].setText(String.valueOf(defaultFrames[i]));
        }
        String[] defaultAddrs = {"14348", "22440", "6200", "45056"};
        for (String addr : defaultAddrs) {
            addAddressRow(addr);
        }

        // Setup Two-Level defaults
        rebuildTwoLevelDirectory();
        int[][] defaultTwoLevel = {
                {2, 5, 8, 1},
                {7, 3, 10, 4},
                {0, 6, 9, 11},
                {12, 13, 14, 15}
        };
        for (int o = 0; o < defaultTwoLevel.length && o < twoLevelFrameFields.length; o++) {
            for (int in = 0; in < defaultTwoLevel[o].length && in < twoLevelFrameFields[o].length; in++) {
                twoLevelFrameFields[o][in].setText(String.valueOf(defaultTwoLevel[o][in]));
            }
        }
        String[] defaultTwoLevelAddrs = {"5120", "1, 2, 500", "0, 3, 250", "2, 1, 1500"};
        for (String addr : defaultTwoLevelAddrs) {
            addTwoLevelAddressRow(addr);
        }

        // Run initial translation
        runTranslation();
    }

    // ----------------------------------------------------------------
    //  Header & Mode Selector
    // ----------------------------------------------------------------
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("Paging");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Label desc = new Label("Analyze single-level and multi-level (hierarchical) page table mappings and address translations.");
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

    private HBox createModeTabBar() {
        HBox tabBar = new HBox(12);
        tabBar.setAlignment(Pos.CENTER_LEFT);

        Label modeLabel = new Label("Architecture Mode:");
        modeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        singleLevelTab = new Button("Single-Level Paging");
        singleLevelTab.getStyleClass().addAll("algo-tab", "algo-tab-active");
        singleLevelTab.setOnAction(e -> selectMode("SINGLE"));

        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");

        twoLevelTab = new Button("Two-Level Hierarchical Paging");
        twoLevelTab.getStyleClass().add("algo-tab");
        twoLevelTab.setOnAction(e -> selectMode("TWO_LEVEL"));

        tabBar.getChildren().addAll(modeLabel, singleLevelTab, sep, twoLevelTab);
        return tabBar;
    }

    private void selectMode(String mode) {
        this.pagingMode = mode;
        if ("SINGLE".equals(mode)) {
            singleLevelTab.getStyleClass().add("algo-tab-active");
            twoLevelTab.getStyleClass().remove("algo-tab-active");
            singleLevelMainArea.setVisible(true);
            singleLevelMainArea.setManaged(true);
            twoLevelMainArea.setVisible(false);
            twoLevelMainArea.setManaged(false);
            logicalPagesCardTitle.setText("LOGICAL PAGES");
            runTranslation();
        } else {
            twoLevelTab.getStyleClass().add("algo-tab-active");
            singleLevelTab.getStyleClass().remove("algo-tab-active");
            singleLevelMainArea.setVisible(false);
            singleLevelMainArea.setManaged(false);
            twoLevelMainArea.setVisible(true);
            twoLevelMainArea.setManaged(true);
            logicalPagesCardTitle.setText("PAGE DIRECTORIES");
            runTwoLevelTranslation();
        }
    }

    // ----------------------------------------------------------------
    //  Stat Cards
    // ----------------------------------------------------------------
    private HBox createStatCards() {
        VBox c1 = buildStatCard("PAGE SIZE", pageSizeValue, "System block size", "#3b82f6");
        VBox c2 = buildStatCard(logicalPagesCardTitle, logicalPagesValue, "Address space divisions", "#2dd4bf");
        VBox c3 = buildStatCard("MAPPED FRAME", mappedFrameValue, "Physical RAM target", "#22c55e");
        VBox c4 = buildStatCard("PHYSICAL ADDRESS", physicalAddrValue, "Resolved byte address", "#2dd4bf");

        HBox row = new HBox(14, c1, c2, c3, c4);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);
        return row;
    }

    private VBox buildStatCard(String labelText, Label valueLabel, String descText, String accentColor) {
        return buildStatCard(new Label(labelText), valueLabel, descText, accentColor);
    }

    private VBox buildStatCard(Label titleLabel, Label valueLabel, String descText, String accentColor) {
        titleLabel.getStyleClass().add("stat-card-label");
        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4; -fx-min-width: 8; -fx-min-height: 8; -fx-max-width: 8; -fx-max-height: 8;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(titleLabel, sp, dot);
        topRow.setAlignment(Pos.CENTER);
        valueLabel.getStyleClass().add("stat-card-value");
        Label descLabel = new Label(descText);
        descLabel.getStyleClass().add("stat-card-description");
        descLabel.setStyle("-fx-text-fill: " + accentColor + ";");
        VBox card = new VBox(4, topRow, valueLabel, descLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ================================================================
    //  SINGLE-LEVEL PAGING IMPLEMENTATION
    // ================================================================
    private VBox createSingleLevelMainArea() {
        VBox leftPanel = createPageTablePanel();
        VBox rightPanel = createTranslationPanel();

        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        leftPanel.setMinWidth(360);
        leftPanel.setPrefWidth(420);
        leftPanel.setMaxWidth(500);

        HBox main = new HBox(14, leftPanel, rightPanel);
        main.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(main, Priority.ALWAYS);
        return new VBox(main);
    }

    private VBox createPageTablePanel() {
        Label panelTitle = new Label("Single-Level Page Table (Page No. -> Frame No.)");
        panelTitle.getStyleClass().add("panel-title");
        panelTitle.setStyle("-fx-font-size: 16px;");

        Label psLabel = new Label("Page Size:");
        psLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        pageSizeField.setPrefWidth(80);
        Label pcLabel = new Label("Pages:");
        pcLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        pageCountSpinner.setPrefWidth(70);
        pageCountSpinner.setEditable(true);
        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("btn-outline");
        applyBtn.setOnAction(e -> rebuildPageTable());

        HBox configRow = new HBox(8, psLabel, pageSizeField, pcLabel, pageCountSpinner, applyBtn);
        configRow.setAlignment(Pos.CENTER_LEFT);

        Label pnH = new Label("Page Number");
        pnH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        pnH.setPrefWidth(130);
        Label fnH = new Label("Frame Number");
        fnH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        HBox colH = new HBox(8, pnH, fnH);
        colH.setPadding(new Insets(4, 0, 2, 0));

        ScrollPane tableScroll = new ScrollPane(pageTableRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setPrefHeight(230);
        tableScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Separator sep = new Separator();

        Label addrTitle = new Label("Logical Addresses to Translate");
        addrTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Button addAddrBtn = new Button("+ Add Address");
        addAddrBtn.getStyleClass().add("btn-outline");
        addAddrBtn.setOnAction(e -> addAddressRow());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox addrHeader = new HBox(8, addrTitle, sp, addAddrBtn);
        addrHeader.setAlignment(Pos.CENTER_LEFT);

        ScrollPane addrScroll = new ScrollPane(addressRows);
        addrScroll.setFitToWidth(true);
        addrScroll.setPrefHeight(120);
        addrScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        Button confirmBtn = new Button("Run Translation");
        confirmBtn.getStyleClass().add("btn-accent");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> runTranslation());

        verificationStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf;");
        verificationStatus.setWrapText(true);
        verificationCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

        VBox panel = new VBox(10, panelTitle, configRow, colH, tableScroll, sep,
                addrHeader, addrScroll, errorLabel, confirmBtn, verificationStatus, verificationCount);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void rebuildPageTable() {
        int count = pageCountSpinner.getValue();
        pageTableRows.getChildren().clear();
        frameFields = new TextField[count];

        for (int i = 0; i < count; i++) {
            Label pageLabel = new Label("P" + i);
            pageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
            pageLabel.setPrefWidth(130);

            TextField frameField = new TextField();
            frameField.setPromptText("Frame " + i);
            frameField.setPrefWidth(160);
            frameFields[i] = frameField;

            HBox row = new HBox(8, pageLabel, frameField);
            row.setAlignment(Pos.CENTER_LEFT);
            pageTableRows.getChildren().add(row);
        }

        logicalPagesValue.setText(String.format("%02d", count));
    }

    private void addAddressRow() {
        addAddressRow("");
    }

    private void addAddressRow(String addr) {
        TextField addrField = new TextField(addr);
        addrField.setPromptText("Address (e.g. 14348 or 2, 300)");
        addrField.setPrefWidth(250);

        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6;");

        HBox row = new HBox(8, addrField, delBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(addrField);
        delBtn.setOnAction(e -> {
            if (addressRows.getChildren().size() > 1) addressRows.getChildren().remove(row);
        });

        addressRows.getChildren().add(row);
    }

    private VBox createTranslationPanel() {
        Label flowTitle = new Label("Logical-to-Physical Address Translation");
        flowTitle.getStyleClass().add("panel-title");
        flowTitle.setStyle("-fx-font-size: 16px;");
        Label flowSubtitle = new Label("Flowchart and computational formula");
        flowSubtitle.getStyleClass().add("panel-subtitle");

        translationFlowchart.setPadding(new Insets(4));
        translationFlowchart.setStyle("-fx-alignment: center-left;");

        Label emptyFlow = new Label("Run a translation to see the flowchart");
        emptyFlow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        translationFlowchart.getChildren().add(emptyFlow);

        VBox flowPanel = new VBox(8, flowTitle, flowSubtitle, translationFlowchart);
        flowPanel.getStyleClass().add("panel");

        Label tableTitle = new Label("Address Translation Verification Log");
        tableTitle.getStyleClass().add("panel-title");
        tableTitle.setStyle("-fx-font-size: 16px;");

        setupVerificationTable();

        VBox tablePanel = new VBox(8, tableTitle, verificationTable);
        tablePanel.getStyleClass().add("panel");

        VBox right = new VBox(14, flowPanel, tablePanel);
        VBox.setVgrow(right, Priority.ALWAYS);
        return right;
    }

    @SuppressWarnings("unchecked")
    private void setupVerificationTable() {
        TableColumn<TranslationResult, String> reqCol = new TableColumn<>("Req ID");
        reqCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getRequestId()));
        reqCol.setPrefWidth(55);

        TableColumn<TranslationResult, String> laCol = new TableColumn<>("Logical Address");
        laCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getLogicalAddressDisplay()));
        laCol.setPrefWidth(120);

        TableColumn<TranslationResult, String> pnCol = new TableColumn<>("Page\nNo.");
        pnCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getPageNumber())));
        pnCol.setPrefWidth(50);

        TableColumn<TranslationResult, String> offCol = new TableColumn<>("Offset");
        offCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getOffset())));
        offCol.setPrefWidth(60);

        TableColumn<TranslationResult, String> mfCol = new TableColumn<>("Mapped\nFrame");
        mfCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getFrameDisplay()));
        mfCol.setPrefWidth(80);

        TableColumn<TranslationResult, String> paCol = new TableColumn<>("Physical\nAddress");
        paCol.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().isValid() ? String.valueOf(r.getValue().getPhysicalAddress()) : "-"));
        paCol.setPrefWidth(75);

        TableColumn<TranslationResult, String> statusCol = new TableColumn<>("Validation Status");
        statusCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatus()));
        statusCol.setPrefWidth(160);

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    if (item.contains("SUCCESS")) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        reqCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: #2dd4bf; -fx-font-weight: bold;"); }
            }
        });

        verificationTable.getColumns().addAll(reqCol, laCol, pnCol, offCol, mfCol, paCol, statusCol);
        verificationTable.setPlaceholder(new Label("Run a translation to see results"));
        verificationTable.setPrefHeight(220);
        verificationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void runTranslation() {
        errorLabel.setText("");

        int pageSize;
        try {
            pageSize = Integer.parseInt(pageSizeField.getText().trim());
            if (pageSize <= 0) { errorLabel.setText("Page size must be positive."); return; }
        } catch (NumberFormatException e) {
            errorLabel.setText("Page size must be a valid integer."); return;
        }

        int numPages = frameFields.length;
        int[] pageTable = new int[numPages];
        for (int i = 0; i < numPages; i++) {
            String text = frameFields[i].getText().trim();
            if (text.isEmpty()) { errorLabel.setText("Frame number for P" + i + " is required."); return; }
            try {
                pageTable[i] = Integer.parseInt(text);
                if (pageTable[i] < 0) { errorLabel.setText("Frame for P" + i + " must be non-negative."); return; }
            } catch (NumberFormatException e) {
                errorLabel.setText("Frame for P" + i + " must be a valid integer."); return;
            }
        }

        List<PagingEngine.PagingRequest> requests = new ArrayList<>();
        for (int i = 0; i < addressRows.getChildren().size(); i++) {
            HBox row = (HBox) addressRows.getChildren().get(i);
            TextField field = (TextField) row.getUserData();
            String text = field.getText().trim();
            if (text.isEmpty()) continue;

            String stripped = text.replaceAll("[()a-zA-Z=]", " ").trim();
            String[] parts = stripped.split("[,\\s:]+");
            if (parts.length == 2) {
                try {
                    int p = Integer.parseInt(parts[0]);
                    int off = Integer.parseInt(parts[1]);
                    requests.add(new PagingEngine.PagingRequest(p, off));
                } catch (NumberFormatException e) {
                    errorLabel.setText("Address #" + (i + 1) + " must be a valid integer or 'page, offset'.");
                    return;
                }
            } else if (parts.length == 1) {
                try {
                    int addr = Integer.parseInt(parts[0]);
                    requests.add(new PagingEngine.PagingRequest(addr));
                } catch (NumberFormatException e) {
                    errorLabel.setText("Address #" + (i + 1) + " must be a valid integer or 'page, offset'.");
                    return;
                }
            } else {
                errorLabel.setText("Address #" + (i + 1) + " format invalid. Use e.g. 14348 or 2, 300.");
                return;
            }
        }

        if (requests.isEmpty()) {
            errorLabel.setText("At least one address is required."); return;
        }

        List<String> errors = engine.validate(pageSize, pageTable);
        if (!errors.isEmpty()) { errorLabel.setText(String.join("\n", errors)); return; }

        session.setSimulationState(SimulationState.RUNNING);
        PagingOutput output = engine.translateRequests(pageSize, pageTable, requests);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        pageSizeValue.setText(pageSize + " B");
        logicalPagesValue.setText(String.format("%02d", numPages));

        TranslationResult lastValid = null;
        for (TranslationResult t : output.getTranslations()) {
            if (t.isValid()) lastValid = t;
        }
        if (lastValid != null) {
            mappedFrameValue.setText("Frame " + lastValid.getFrameNumber());
            physicalAddrValue.setText(String.valueOf(lastValid.getPhysicalAddress()));
        } else {
            mappedFrameValue.setText("—");
            physicalAddrValue.setText("—");
        }

        updateFlowchart(output, pageSize);
        verificationTable.setItems(FXCollections.observableArrayList(output.getTranslations()));

        verificationStatus.setText("Verification Complete: All translation boundary rules and exception checks verified.");
        verificationCount.setText("Translation Verification count: " + output.getTranslationCount() + " cases.");
    }

    private void updateFlowchart(PagingOutput output, int pageSize) {
        translationFlowchart.getChildren().clear();
        if (output.getTranslations().isEmpty()) return;

        TranslationResult first = output.getTranslations().get(0);

        Label inputLabel = new Label("INPUT LOGICAL ADDRESS: Page " + first.getPageNumber()
                + ", Offset " + first.getOffset());
        inputLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

        Label pageBox = new Label("Page: " + first.getPageNumber());
        pageBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #f8fafc; -fx-background-color: #1e293b; " +
                "-fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12;");
        Label offsetBox = new Label("Offset: " + first.getOffset());
        offsetBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #f8fafc; -fx-background-color: #1e293b; " +
                "-fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12;");
        HBox addrBoxes = new HBox(8, pageBox, offsetBox);

        Label arrow1 = new Label("    ↓  PAGE TABLE LOOKUP (Frame = pageTable[Page])");
        arrow1.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        if (first.isValid()) {
            Label formula = new Label("Formula: (" + first.getFrameNumber() + " × " + pageSize
                    + ") + " + first.getOffset() + " = " + first.getPhysicalAddress());
            formula.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

            Label validation = new Label("Offset " + first.getOffset() + " < Page Size " + pageSize + " (VALID)");
            validation.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            Label resultLabel = new Label("PHYSICAL ADDRESS: " + first.getPhysicalAddress());
            resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#2dd4bf, -85%); -fx-background-radius: 6;");

            translationFlowchart.getChildren().addAll(inputLabel, addrBoxes, arrow1, formula, validation, resultLabel);
        } else {
            Label trapLabel = new Label(first.getStatus());
            trapLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#ef4444, -85%); -fx-background-radius: 6;");

            translationFlowchart.getChildren().addAll(inputLabel, addrBoxes, arrow1, trapLabel);
        }
    }

    // ================================================================
    //  TWO-LEVEL (MULTI-LEVEL) HIERARCHICAL PAGING IMPLEMENTATION
    // ================================================================
    private VBox createTwoLevelMainArea() {
        VBox leftPanel = createTwoLevelDirectoryPanel();
        VBox rightPanel = createTwoLevelTranslationPanel();

        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        leftPanel.setMinWidth(380);
        leftPanel.setPrefWidth(450);
        leftPanel.setMaxWidth(520);

        HBox main = new HBox(14, leftPanel, rightPanel);
        main.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(main, Priority.ALWAYS);
        return new VBox(main);
    }

    private VBox createTwoLevelDirectoryPanel() {
        Label panelTitle = new Label("Two-Level Hierarchical Page Directory");
        panelTitle.getStyleClass().add("panel-title");
        panelTitle.setStyle("-fx-font-size: 16px;");

        Label psLabel = new Label("Page Size:");
        psLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        twoLevelPageSizeField.setPrefWidth(70);

        Label dirLabel = new Label("Outer (p1):");
        dirLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        outerCountSpinner.setPrefWidth(60);
        outerCountSpinner.setEditable(true);

        Label inLabel = new Label("Inner (p2):");
        inLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        innerCountSpinner.setPrefWidth(60);
        innerCountSpinner.setEditable(true);

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("btn-outline");
        applyBtn.setOnAction(e -> rebuildTwoLevelDirectory());

        HBox configRow = new HBox(6, psLabel, twoLevelPageSizeField, dirLabel, outerCountSpinner, inLabel, innerCountSpinner, applyBtn);
        configRow.setAlignment(Pos.CENTER_LEFT);

        ScrollPane dirScroll = new ScrollPane(twoLevelDirectoryRows);
        dirScroll.setFitToWidth(true);
        dirScroll.setPrefHeight(230);
        dirScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Separator sep = new Separator();

        Label addrTitle = new Label("Logical Addresses / (p1, p2, offset)");
        addrTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Button addAddrBtn = new Button("+ Add Address");
        addAddrBtn.getStyleClass().add("btn-outline");
        addAddrBtn.setOnAction(e -> addTwoLevelAddressRow());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox addrHeader = new HBox(8, addrTitle, sp, addAddrBtn);
        addrHeader.setAlignment(Pos.CENTER_LEFT);

        ScrollPane addrScroll = new ScrollPane(twoLevelAddressRows);
        addrScroll.setFitToWidth(true);
        addrScroll.setPrefHeight(120);
        addrScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        twoLevelErrorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        twoLevelErrorLabel.setWrapText(true);

        Button confirmBtn = new Button("Run Two-Level Translation");
        confirmBtn.getStyleClass().add("btn-accent");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> runTwoLevelTranslation());

        twoLevelVerificationStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf;");
        twoLevelVerificationStatus.setWrapText(true);
        twoLevelVerificationCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

        VBox panel = new VBox(10, panelTitle, configRow, dirScroll, sep,
                addrHeader, addrScroll, twoLevelErrorLabel, confirmBtn,
                twoLevelVerificationStatus, twoLevelVerificationCount);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void rebuildTwoLevelDirectory() {
        int outerCount = outerCountSpinner.getValue();
        int innerCount = innerCountSpinner.getValue();

        twoLevelDirectoryRows.getChildren().clear();
        twoLevelFrameFields = new TextField[outerCount][innerCount];

        for (int o = 0; o < outerCount; o++) {
            VBox dirBox = new VBox(6);
            dirBox.setStyle("-fx-background-color: #161b22; -fx-background-radius: 6; " +
                    "-fx-padding: 8; -fx-border-color: #30363d; -fx-border-radius: 6;");

            Label dirHeader = new Label("Page Directory " + o + " (Outer p1 = " + o + ")");
            dirHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
            dirBox.getChildren().add(dirHeader);

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(4);

            for (int in = 0; in < innerCount; in++) {
                Label pLabel = new Label("P" + in + " → Frame:");
                pLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

                TextField fField = new TextField();
                fField.setPromptText("Frame");
                fField.setPrefWidth(65);
                twoLevelFrameFields[o][in] = fField;

                grid.add(pLabel, (in % 2) * 2, in / 2);
                grid.add(fField, (in % 2) * 2 + 1, in / 2);
            }

            dirBox.getChildren().add(grid);
            twoLevelDirectoryRows.getChildren().add(dirBox);
        }

        logicalPagesValue.setText(String.format("%02d", outerCount));
    }

    private void addTwoLevelAddressRow() {
        addTwoLevelAddressRow("");
    }

    private void addTwoLevelAddressRow(String addr) {
        TextField addrField = new TextField(addr);
        addrField.setPromptText("Address (e.g. 5120 or 1, 2, 500)");
        addrField.setPrefWidth(260);

        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6;");

        HBox row = new HBox(8, addrField, delBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(addrField);
        delBtn.setOnAction(e -> {
            if (twoLevelAddressRows.getChildren().size() > 1) {
                twoLevelAddressRows.getChildren().remove(row);
            }
        });

        twoLevelAddressRows.getChildren().add(row);
    }

    private VBox createTwoLevelTranslationPanel() {
        Label flowTitle = new Label("Two-Level Address Translation Hierarchy");
        flowTitle.getStyleClass().add("panel-title");
        flowTitle.setStyle("-fx-font-size: 16px;");
        Label flowSubtitle = new Label("Multi-stage Page Directory and Inner Table resolution");
        flowSubtitle.getStyleClass().add("panel-subtitle");

        twoLevelFlowchart.setPadding(new Insets(4));
        twoLevelFlowchart.setStyle("-fx-alignment: center-left;");

        Label emptyFlow = new Label("Run a translation to see the multi-level flowchart");
        emptyFlow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        twoLevelFlowchart.getChildren().add(emptyFlow);

        VBox flowPanel = new VBox(8, flowTitle, flowSubtitle, twoLevelFlowchart);
        flowPanel.getStyleClass().add("panel");

        Label tableTitle = new Label("Two-Level Translation Verification Log");
        tableTitle.getStyleClass().add("panel-title");
        tableTitle.setStyle("-fx-font-size: 16px;");

        setupTwoLevelVerificationTable();

        VBox tablePanel = new VBox(8, tableTitle, twoLevelVerificationTable);
        tablePanel.getStyleClass().add("panel");

        VBox right = new VBox(14, flowPanel, tablePanel);
        VBox.setVgrow(right, Priority.ALWAYS);
        return right;
    }

    @SuppressWarnings("unchecked")
    private void setupTwoLevelVerificationTable() {
        TableColumn<TwoLevelTranslationResult, String> reqCol = new TableColumn<>("Req");
        reqCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getRequestId()));
        reqCol.setPrefWidth(45);

        TableColumn<TwoLevelTranslationResult, String> laCol = new TableColumn<>("Logical Address");
        laCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getLogicalAddressDisplay()));
        laCol.setPrefWidth(125);

        TableColumn<TwoLevelTranslationResult, String> p1Col = new TableColumn<>("Outer\n(p1)");
        p1Col.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getOuterPage())));
        p1Col.setPrefWidth(55);

        TableColumn<TwoLevelTranslationResult, String> p2Col = new TableColumn<>("Inner\n(p2)");
        p2Col.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getInnerPage())));
        p2Col.setPrefWidth(55);

        TableColumn<TwoLevelTranslationResult, String> offCol = new TableColumn<>("Offset\n(d)");
        offCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getOffset())));
        offCol.setPrefWidth(60);

        TableColumn<TwoLevelTranslationResult, String> mfCol = new TableColumn<>("Mapped\nFrame");
        mfCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getFrameDisplay()));
        mfCol.setPrefWidth(75);

        TableColumn<TwoLevelTranslationResult, String> paCol = new TableColumn<>("Physical\nAddress");
        paCol.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().isValid() ? String.valueOf(r.getValue().getPhysicalAddress()) : "-"));
        paCol.setPrefWidth(75);

        TableColumn<TwoLevelTranslationResult, String> statusCol = new TableColumn<>("Validation Status");
        statusCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatus()));
        statusCol.setPrefWidth(150);

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    if (item.contains("SUCCESS")) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        reqCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: #2dd4bf; -fx-font-weight: bold;"); }
            }
        });

        twoLevelVerificationTable.getColumns().addAll(reqCol, laCol, p1Col, p2Col, offCol, mfCol, paCol, statusCol);
        twoLevelVerificationTable.setPlaceholder(new Label("Run a translation to see results"));
        twoLevelVerificationTable.setPrefHeight(220);
        twoLevelVerificationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void runTwoLevelTranslation() {
        twoLevelErrorLabel.setText("");

        int pageSize;
        try {
            pageSize = Integer.parseInt(twoLevelPageSizeField.getText().trim());
            if (pageSize <= 0) { twoLevelErrorLabel.setText("Page size must be positive."); return; }
        } catch (NumberFormatException e) {
            twoLevelErrorLabel.setText("Page size must be a valid integer."); return;
        }

        int outerCount = twoLevelFrameFields.length;
        int innerCount = twoLevelFrameFields[0].length;
        int[][] directory = new int[outerCount][innerCount];

        for (int o = 0; o < outerCount; o++) {
            for (int in = 0; in < innerCount; in++) {
                String text = twoLevelFrameFields[o][in].getText().trim();
                if (text.isEmpty()) {
                    twoLevelErrorLabel.setText("Frame number for Dir " + o + ", Page " + in + " is required.");
                    return;
                }
                try {
                    directory[o][in] = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    twoLevelErrorLabel.setText("Invalid frame at Dir " + o + ", Page " + in);
                    return;
                }
            }
        }

        List<PagingEngine.TwoLevelPagingRequest> requests = new ArrayList<>();
        for (int i = 0; i < twoLevelAddressRows.getChildren().size(); i++) {
            HBox row = (HBox) twoLevelAddressRows.getChildren().get(i);
            TextField field = (TextField) row.getUserData();
            String text = field.getText().trim();
            if (text.isEmpty()) continue;

            String stripped = text.replaceAll("[()a-zA-Z=]", " ").trim();
            String[] parts = stripped.split("[,\\s:]+");
            if (parts.length == 3) {
                try {
                    int p1 = Integer.parseInt(parts[0]);
                    int p2 = Integer.parseInt(parts[1]);
                    int off = Integer.parseInt(parts[2]);
                    requests.add(new PagingEngine.TwoLevelPagingRequest(p1, p2, off));
                } catch (NumberFormatException e) {
                    twoLevelErrorLabel.setText("Address #" + (i + 1) + " must be a valid 'p1, p2, offset'.");
                    return;
                }
            } else if (parts.length == 1) {
                try {
                    int addr = Integer.parseInt(parts[0]);
                    requests.add(new PagingEngine.TwoLevelPagingRequest(addr));
                } catch (NumberFormatException e) {
                    twoLevelErrorLabel.setText("Address #" + (i + 1) + " must be a valid integer or 'p1, p2, offset'.");
                    return;
                }
            } else {
                twoLevelErrorLabel.setText("Address #" + (i + 1) + " format invalid. Use e.g. 5120 or 1, 2, 500.");
                return;
            }
        }

        if (requests.isEmpty()) {
            twoLevelErrorLabel.setText("At least one address is required."); return;
        }

        session.setSimulationState(SimulationState.RUNNING);
        TwoLevelPagingOutput output = engine.translateTwoLevel(pageSize, innerCount, directory, requests);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        pageSizeValue.setText(pageSize + " B");
        logicalPagesValue.setText(String.format("%02d", outerCount));

        TwoLevelTranslationResult lastValid = null;
        for (TwoLevelTranslationResult t : output.getTranslations()) {
            if (t.isValid()) lastValid = t;
        }
        if (lastValid != null) {
            mappedFrameValue.setText("Frame " + lastValid.getFrameNumber());
            physicalAddrValue.setText(String.valueOf(lastValid.getPhysicalAddress()));
        } else {
            mappedFrameValue.setText("—");
            physicalAddrValue.setText("—");
        }

        updateTwoLevelFlowchart(output, pageSize);
        twoLevelVerificationTable.setItems(FXCollections.observableArrayList(output.getTranslations()));

        twoLevelVerificationStatus.setText("Hierarchical Verification Complete: Checked Page Directory, Inner Page Tables, and bounds.");
        twoLevelVerificationCount.setText("Multi-Level Translation Verification count: " + output.getTranslationCount() + " cases.");
    }

    private void updateTwoLevelFlowchart(TwoLevelPagingOutput output, int pageSize) {
        twoLevelFlowchart.getChildren().clear();
        if (output.getTranslations().isEmpty()) return;

        TwoLevelTranslationResult first = output.getTranslations().get(0);

        Label inputLabel = new Label("INPUT LOGICAL ADDRESS: [Outer p1 = " + first.getOuterPage()
                + " | Inner p2 = " + first.getInnerPage() + " | Offset = " + first.getOffset() + "]");
        inputLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Label p1Box = new Label("p1 (Dir): " + first.getOuterPage());
        p1Box.setStyle("-fx-font-size: 12px; -fx-text-fill: #38bdf8; -fx-background-color: #1e293b; " +
                "-fx-border-color: #0284c7; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        Label p2Box = new Label("p2 (Table): " + first.getInnerPage());
        p2Box.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-background-color: #1e293b; " +
                "-fx-border-color: #0d9488; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        Label offsetBox = new Label("Offset (d): " + first.getOffset());
        offsetBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #f8fafc; -fx-background-color: #1e293b; " +
                "-fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        HBox addrBoxes = new HBox(8, p1Box, p2Box, offsetBox);

        Label stage1 = new Label("    ↓  STAGE 1: Look up Page Directory [p1 = " + first.getOuterPage() + "] → Points to Inner Page Table");
        stage1.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Label stage2 = new Label("    ↓  STAGE 2: Look up Inner Page Table [p2 = " + first.getInnerPage() + "] → Frame Number");
        stage2.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        if (first.isValid()) {
            Label formula = new Label("Formula: (" + first.getFrameNumber() + " × " + pageSize
                    + ") + " + first.getOffset() + " = " + first.getPhysicalAddress());
            formula.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

            Label resultLabel = new Label("RESOLVED PHYSICAL ADDRESS: " + first.getPhysicalAddress() + " (Frame " + first.getFrameNumber() + ")");
            resultLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#2dd4bf, -85%); -fx-background-radius: 6;");

            twoLevelFlowchart.getChildren().addAll(inputLabel, addrBoxes, stage1, stage2, formula, resultLabel);
        } else {
            Label trapLabel = new Label(first.getStatus());
            trapLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#ef4444, -85%); -fx-background-radius: 6;");

            twoLevelFlowchart.getChildren().addAll(inputLabel, addrBoxes, stage1, stage2, trapLabel);
        }
    }

    private void resetAll() {
        if ("SINGLE".equals(pagingMode)) {
            rebuildPageTable();
            addressRows.getChildren().clear();
            for (int i = 0; i < 4; i++) addAddressRow();
            verificationTable.getItems().clear();
            translationFlowchart.getChildren().clear();
            Label emptyFlow = new Label("Run a translation to see the flowchart");
            emptyFlow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            translationFlowchart.getChildren().add(emptyFlow);
            verificationStatus.setText("");
            verificationCount.setText("");
            errorLabel.setText("");
        } else {
            rebuildTwoLevelDirectory();
            twoLevelAddressRows.getChildren().clear();
            for (int i = 0; i < 4; i++) addTwoLevelAddressRow();
            twoLevelVerificationTable.getItems().clear();
            twoLevelFlowchart.getChildren().clear();
            Label emptyFlow = new Label("Run a translation to see the multi-level flowchart");
            emptyFlow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            twoLevelFlowchart.getChildren().add(emptyFlow);
            twoLevelVerificationStatus.setText("");
            twoLevelVerificationCount.setText("");
            twoLevelErrorLabel.setText("");
        }
        mappedFrameValue.setText("—");
        physicalAddrValue.setText("—");
        session.setSimulationState(SimulationState.IDLE);
    }
}
