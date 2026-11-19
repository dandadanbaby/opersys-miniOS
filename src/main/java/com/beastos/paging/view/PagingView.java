package com.beastos.paging.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.paging.engine.PagingEngine;
import com.beastos.paging.model.PagingOutput;
import com.beastos.paging.model.TranslationResult;

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
 * Matches design: page map table, logical-to-physical translation flowchart,
 * and address translation verification log.
 */
public class PagingView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final PagingEngine engine = new PagingEngine();

    // Config
    private final TextField pageSizeField = new TextField("4096");
    private final Spinner<Integer> pageCountSpinner = new Spinner<>(1, 100, 8);

    // Page table input
    private final VBox pageTableRows = new VBox(4);
    private TextField[] frameFields;

    // Logical address input
    private final VBox addressRows = new VBox(4);

    // Stat cards
    private final Label pageSizeValue = new Label("4096 B");
    private final Label logicalPagesValue = new Label("08");
    private final Label mappedFrameValue = new Label("—");
    private final Label physicalAddrValue = new Label("—");

    // Results
    private final VBox translationFlowchart = new VBox(10);
    private final TableView<TranslationResult> verificationTable = new TableView<>();
    private final Label verificationStatus = new Label();
    private final Label verificationCount = new Label();
    private final Label errorLabel = new Label();

    public PagingView() {
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

        rebuildPageTable();
        // Load default frame mappings from paging.png
        int[] defaultFrames = {5, 2, 9, 3, 12, 15, 0, 7};
        for (int i = 0; i < defaultFrames.length && i < frameFields.length; i++) {
            frameFields[i].setText(String.valueOf(defaultFrames[i]));
        }

        // Add 4 default benchmark addresses
        String[] defaultAddrs = {"14348", "22440", "6200", "45056"};
        for (String addr : defaultAddrs) {
            addAddressRow(addr);
        }

        // Run translation on launch
        runTranslation();
    }

    // ----------------------------------------------------------------
    //  Header
    // ----------------------------------------------------------------
    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");

        Label title = new Label("Paging");
        title.getStyleClass().add("content-title");

        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");

        Label desc = new Label("Analyze page-to-frame mapping and logical-to-physical address translation.");
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
        VBox c1 = buildStatCard("PAGE SIZE", pageSizeValue, "System block size", "#3b82f6");
        VBox c2 = buildStatCard("LOGICAL PAGES", logicalPagesValue, "Process address space", "#2dd4bf");
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
    //  Main Area — two columns
    // ----------------------------------------------------------------
    private HBox createMainArea() {
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
        return main;
    }

    // ----------------------------------------------------------------
    //  Left Panel — Page Map Table + Address Input
    // ----------------------------------------------------------------
    private VBox createPageTablePanel() {
        Label panelTitle = new Label("Page Map Table (Page No. -> Frame No.)");
        panelTitle.getStyleClass().add("panel-title");
        panelTitle.setStyle("-fx-font-size: 16px;");

        // Config row
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

        // Column headers
        Label pnH = new Label("Page Number");
        pnH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        pnH.setPrefWidth(130);
        Label fnH = new Label("Frame Number");
        fnH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        HBox colH = new HBox(8, pnH, fnH);
        colH.setPadding(new Insets(4, 0, 2, 0));

        ScrollPane tableScroll = new ScrollPane(pageTableRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setPrefHeight(250);
        tableScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Separator
        Separator sep = new Separator();

        // Logical address input section
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

        // Error label
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        // Confirm button
        Button confirmBtn = new Button("Confirm");
        confirmBtn.getStyleClass().add("btn-accent");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> runTranslation());

        // Verification footer
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

    // ----------------------------------------------------------------
    //  Right Panel — Translation Flowchart + Verification Log
    // ----------------------------------------------------------------
    private VBox createTranslationPanel() {
        // Flowchart panel
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

        // Verification table
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

        // Color-code status
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

        // Color PID column
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

    // ----------------------------------------------------------------
    //  Simulation Execution
    // ----------------------------------------------------------------
    private void runTranslation() {
        errorLabel.setText("");

        // Parse page size
        int pageSize;
        try {
            pageSize = Integer.parseInt(pageSizeField.getText().trim());
            if (pageSize <= 0) { errorLabel.setText("Page size must be positive."); return; }
        } catch (NumberFormatException e) {
            errorLabel.setText("Page size must be a valid integer."); return;
        }

        // Parse page table
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

        // Parse logical addresses or (page, offset)
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

        // Validate
        List<String> errors = engine.validate(pageSize, pageTable);
        if (!errors.isEmpty()) { errorLabel.setText(String.join("\n", errors)); return; }

        // Run translation
        session.setSimulationState(SimulationState.RUNNING);
        PagingOutput output = engine.translateRequests(pageSize, pageTable, requests);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        // Update stat cards
        pageSizeValue.setText(pageSize + " B");
        logicalPagesValue.setText(String.format("%02d", numPages));

        // Show most recent valid translation in stat cards
        TranslationResult lastValid = null;
        for (TranslationResult t : output.getTranslations()) {
            if (t.isValid()) lastValid = t;
        }
        if (lastValid != null) {
            mappedFrameValue.setText("Frame " + lastValid.getFrameNumber());
            physicalAddrValue.setText(String.valueOf(lastValid.getPhysicalAddress()));
        }

        // Update flowchart — show the first translation as example
        updateFlowchart(output, pageSize);

        // Update verification table
        verificationTable.setItems(FXCollections.observableArrayList(output.getTranslations()));

        // Footer
        verificationStatus.setText("Verification Complete: All translation boundary rules and exception checks verified.");
        verificationCount.setText("Translation Verification count: " + output.getTranslationCount() + " cases.");
    }

    private void updateFlowchart(PagingOutput output, int pageSize) {
        translationFlowchart.getChildren().clear();

        if (output.getTranslations().isEmpty()) return;

        TranslationResult first = output.getTranslations().get(0);

        // Input
        Label inputLabel = new Label("INPUT LOGICAL ADDRESS: Page " + first.getPageNumber()
                + ", Offset " + first.getOffset());
        inputLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");

        // Page/Offset boxes
        Label pageBox = new Label("Page: " + first.getPageNumber());
        pageBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #f8fafc; -fx-background-color: #1e293b; " +
                "-fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12;");
        Label offsetBox = new Label("Offset: " + first.getOffset());
        offsetBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #f8fafc; -fx-background-color: #1e293b; " +
                "-fx-border-color: #334155; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12;");
        HBox addrBoxes = new HBox(8, pageBox, offsetBox);

        // Arrow + formula
        Label arrow1 = new Label("    ↓  PAGE TABLE CHECK");
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

    private void resetAll() {
        rebuildPageTable();
        addressRows.getChildren().clear();
        for (int i = 0; i < 4; i++) addAddressRow();
        verificationTable.getItems().clear();
        translationFlowchart.getChildren().clear();
        Label emptyFlow = new Label("Run a translation to see the flowchart");
        emptyFlow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        translationFlowchart.getChildren().add(emptyFlow);
        mappedFrameValue.setText("—");
        physicalAddrValue.setText("—");
        verificationStatus.setText("");
        verificationCount.setText("");
        errorLabel.setText("");
        session.setSimulationState(SimulationState.IDLE);
    }
}
