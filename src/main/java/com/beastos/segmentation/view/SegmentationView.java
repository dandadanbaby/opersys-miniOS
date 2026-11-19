package com.beastos.segmentation.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.segmentation.engine.SegmentationEngine;
import com.beastos.segmentation.model.Segment;
import com.beastos.segmentation.model.SegmentTranslationResult;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Segmentation module view for BEAST OS.
 * Matches design: segment table, physical memory map, validation & trap log.
 */
public class SegmentationView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final SegmentationEngine engine = new SegmentationEngine();

    private static final Color[] SEG_COLORS = {
            Color.web("#22c55e"), Color.web("#f59e0b"), Color.web("#2dd4bf"),
            Color.web("#3b82f6"), Color.web("#a855f7"), Color.web("#ef4444"),
            Color.web("#ec4899"), Color.web("#14716a")
    };

    // Config
    private final Spinner<Integer> segCountSpinner = new Spinner<>(1, 20, 5);

    // Segment table input
    private final VBox segmentRows = new VBox(4);
    private TextField[] baseFields;
    private TextField[] limitFields;

    // Address input
    private final TextField segNumField = new TextField();
    private final TextField offsetField = new TextField();

    // Stat cards
    private final Label totalSegValue = new Label("05");
    private final Label targetSegValue = new Label("—");
    private final Label validationValue = new Label("—");
    private final Label physAddrValue = new Label("—");

    // Results
    private final Canvas memoryMap = new Canvas(460, 340);
    private final VBox validationLog = new VBox(6);
    private final Label errorLabel = new Label();

    public SegmentationView() {
        VBox content = new VBox(20);
        content.getChildren().add(createHeader());
        content.getChildren().add(createStatCards());
        content.getChildren().add(createMainArea());
        content.getChildren().add(createValidationPanel());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        rebuildSegmentTable();
        // Load default target from segmentation.png
        segNumField.setText("1");
        offsetField.setText("320");
        runTranslation();
    }

    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");
        Label title = new Label("Segmentation");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");
        Label desc = new Label("Segment register mapping, logical-to-physical translation, and trap detection.");
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

    private HBox createStatCards() {
        VBox c1 = buildStatCard("TOTAL SEGMENTS", totalSegValue, "Seg/count", "#3b82f6");
        VBox c2 = buildStatCard("TARGET SEGMENT", targetSegValue, "Active evaluation", "#2dd4bf");
        VBox c3 = buildStatCard("VALIDATION", validationValue, "Boundary check: pass", "#22c55e");
        VBox c4 = buildStatCard("PHYSICAL ADDRESS", physAddrValue, "Calculated physical byte", "#2dd4bf");

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

    private HBox createMainArea() {
        VBox leftPanel = createSegmentTablePanel();
        VBox rightPanel = createMemoryMapPanel();

        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        leftPanel.setMinWidth(380);
        leftPanel.setPrefWidth(440);
        leftPanel.setMaxWidth(520);

        return new HBox(14, leftPanel, rightPanel);
    }

    private VBox createSegmentTablePanel() {
        Label panelTitle = new Label("Segment Table");
        panelTitle.getStyleClass().add("panel-title");
        Label panelSub = new Label("Segment Table Configuration (Segment | Base Address | Limit)");
        panelSub.getStyleClass().add("panel-subtitle");

        // Config row
        Label cLabel = new Label("Segments:");
        cLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        segCountSpinner.setPrefWidth(70);
        segCountSpinner.setEditable(true);
        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("btn-outline");
        applyBtn.setOnAction(e -> rebuildSegmentTable());
        HBox configRow = new HBox(8, cLabel, segCountSpinner, applyBtn);
        configRow.setAlignment(Pos.CENTER_LEFT);

        // Column headers
        Label snH = new Label("Segment No.");
        snH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        snH.setPrefWidth(100);
        Label baH = new Label("Base Address");
        baH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        baH.setPrefWidth(120);
        Label liH = new Label("Limit (Length)");
        liH.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        HBox colH = new HBox(8, snH, baH, liH);

        ScrollPane tableScroll = new ScrollPane(segmentRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setPrefHeight(200);
        tableScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Separator sep = new Separator();

        // Address translation input
        Label addrTitle = new Label("Translate Logical Address");
        addrTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label snLabel = new Label("Segment #:");
        snLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        segNumField.setPrefWidth(70);
        segNumField.setPromptText("1");
        Label offLabel = new Label("Offset:");
        offLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        offsetField.setPrefWidth(80);
        offsetField.setPromptText("320");
        HBox addrRow = new HBox(8, snLabel, segNumField, offLabel, offsetField);
        addrRow.setAlignment(Pos.CENTER_LEFT);

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        Button confirmBtn = new Button("Translate");
        confirmBtn.getStyleClass().add("btn-accent");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> runTranslation());

        VBox panel = new VBox(10, panelTitle, panelSub, configRow, colH, tableScroll,
                sep, addrTitle, addrRow, errorLabel, confirmBtn);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void rebuildSegmentTable() {
        int count = segCountSpinner.getValue();
        segmentRows.getChildren().clear();
        baseFields = new TextField[count];
        limitFields = new TextField[count];

        int[][] defaultData = {
            {1400, 100},
            {6300, 400},
            {4300, 500},
            {2000, 250},
            {7200, 150}
        };

        for (int i = 0; i < count; i++) {
            Label segLabel = new Label("Seg " + i);
            segLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
            segLabel.setPrefWidth(100);

            TextField baseF = new TextField();
            baseF.setPrefWidth(120);
            baseF.setPromptText("Base");
            if (i < defaultData.length) {
                baseF.setText(String.valueOf(defaultData[i][0]));
            }
            baseFields[i] = baseF;

            TextField limitF = new TextField();
            limitF.setPrefWidth(120);
            limitF.setPromptText("Limit");
            if (i < defaultData.length) {
                limitF.setText(String.valueOf(defaultData[i][1]));
            }
            limitFields[i] = limitF;

            HBox row = new HBox(8, segLabel, baseF, limitF);
            row.setAlignment(Pos.CENTER_LEFT);
            segmentRows.getChildren().add(row);
        }
        totalSegValue.setText(String.format("%02d", count));
    }

    private VBox createMemoryMapPanel() {
        Label title = new Label("Physical Memory Map");
        title.getStyleClass().add("panel-title");
        Label sub = new Label("Segments mapped proportionally in RAM");
        sub.getStyleClass().add("panel-subtitle");

        drawEmptyMemoryMap();

        memoryMap.widthProperty().addListener((o, a, b) -> drawEmptyMemoryMap());

        VBox panel = new VBox(8, title, sub, memoryMap);
        panel.getStyleClass().add("panel");

        memoryMap.widthProperty().bind(panel.widthProperty().subtract(40));

        return panel;
    }

    private VBox createValidationPanel() {
        Label title = new Label("Logical Address Validation & Trap Log");
        title.getStyleClass().add("panel-title");

        ScrollPane logScroll = new ScrollPane(validationLog);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(120);
        logScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox panel = new VBox(8, title, logScroll);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private void drawEmptyMemoryMap() {
        GraphicsContext gc = memoryMap.getGraphicsContext2D();
        gc.clearRect(0, 0, memoryMap.getWidth(), memoryMap.getHeight());
        gc.setFill(Color.web("#64748b"));
        gc.setFont(Font.font("Segoe UI", 14));
        gc.fillText("Configure segments and translate an address", 30, memoryMap.getHeight() / 2);
    }

    private void drawMemoryMap(List<Segment> segments) {
        GraphicsContext gc = memoryMap.getGraphicsContext2D();
        double w = memoryMap.getWidth();
        double h = memoryMap.getHeight();
        gc.clearRect(0, 0, w, h);

        if (segments.isEmpty()) { drawEmptyMemoryMap(); return; }

        // sort by base address so segments are ordered in the chart
        List<Segment> sorted = new ArrayList<>(segments);
        sorted.sort(Comparator.comparingInt(Segment::getBase));

        int maxAddr = 0;
        for (Segment s : sorted) maxAddr = Math.max(maxAddr, s.getBase() + s.getLimit());
        if (maxAddr == 0) maxAddr = 1;

        // layout margins — left margin is wide enough for Y-axis labels like "Seg 10"
        double leftMargin = 54;
        double rightMargin = 14;
        double topMargin = 10;
        double bottomMargin = 36; // space for X-axis label + ticks
        double chartW = w - leftMargin - rightMargin;
        double chartH = h - topMargin - bottomMargin;
        if (chartH < 40) chartH = 40;

        // each segment gets its own horizontal row (like a horizontal bar chart)
        int segCount = sorted.size();
        double rowH = chartH / segCount;
        double barPad = Math.max(rowH * 0.15, 2); // padding inside each row

        // --- vertical axis (Y-axis) line ---
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1);
        gc.strokeLine(leftMargin, topMargin, leftMargin, topMargin + chartH);

        // --- horizontal axis (X-axis) line ---
        double axisY = topMargin + chartH;
        gc.strokeLine(leftMargin, axisY, leftMargin + chartW, axisY);

        // --- draw segment bars row by row ---
        for (int i = 0; i < segCount; i++) {
            Segment seg = sorted.get(i);
            double rowY = topMargin + i * rowH;

            // Y-axis label for this segment
            gc.setFill(Color.web("#94a3b8"));
            gc.setFont(Font.font("Segoe UI", 11));
            String yLabel = "Seg " + seg.getSegmentNumber();
            gc.fillText(yLabel, 4, rowY + rowH / 2 + 4);

            // Y-axis tick mark
            gc.setStroke(Color.web("#475569"));
            gc.strokeLine(leftMargin - 4, rowY + rowH / 2, leftMargin, rowY + rowH / 2);

            // horizontal grid line (subtle) for each row boundary
            gc.setStroke(Color.web("#1e293b"));
            gc.strokeLine(leftMargin, rowY, leftMargin + chartW, rowY);

            // segment bar positioned according to its base address and limit
            double x = leftMargin + (chartW * seg.getBase() / maxAddr);
            double bw = Math.max(chartW * seg.getLimit() / maxAddr, 4);
            double barY = rowY + barPad;
            double barHeight = rowH - barPad * 2;
            if (barHeight < 4) barHeight = 4;

            Color color = SEG_COLORS[seg.getSegmentNumber() % SEG_COLORS.length];
            gc.setFill(color);
            gc.fillRoundRect(x, barY, bw, barHeight, 4, 4);

            // label inside the bar if there's enough room
            gc.setFill(Color.web("#0b1019"));
            if (bw > 60 && barHeight > 14) {
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                String range = seg.getBase() + "–" + (seg.getBase() + seg.getLimit());
                gc.fillText(range, x + 4, barY + barHeight / 2 + 4);
            } else if (bw > 20 && barHeight > 10) {
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8));
                gc.fillText(String.valueOf(seg.getBase()), x + 2, barY + barHeight / 2 + 3);
            }
        }

        // --- X-axis tick marks and labels (Physical Address) ---
        int tickCount = 5;
        gc.setFont(Font.font("Segoe UI", 9));
        for (int i = 0; i <= tickCount; i++) {
            double x = leftMargin + (chartW * i / tickCount);
            int addrVal = Math.round((float) maxAddr * i / tickCount);
            gc.setStroke(Color.web("#475569"));
            gc.strokeLine(x, axisY, x, axisY + 4);
            String label = String.valueOf(addrVal);
            gc.setFill(Color.web("#64748b"));
            gc.fillText(label, x - label.length() * 3, axisY + 16);
        }

        // X-axis title
        gc.setFont(Font.font("Segoe UI", 10));
        gc.setFill(Color.web("#64748b"));
        String xTitle = "Physical Address";
        gc.fillText(xTitle, leftMargin + chartW / 2 - xTitle.length() * 3, axisY + 30);
    }

    private void runTranslation() {
        errorLabel.setText("");

        // Parse segment table
        int count = baseFields.length;
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String baseStr = baseFields[i].getText().trim();
            String limitStr = limitFields[i].getText().trim();
            if (baseStr.isEmpty() || limitStr.isEmpty()) {
                errorLabel.setText("Seg " + i + ": Base and Limit are required.");
                return;
            }
            try {
                int base = Integer.parseInt(baseStr);
                int limit = Integer.parseInt(limitStr);
                segments.add(new Segment(i, base, limit));
            } catch (NumberFormatException e) {
                errorLabel.setText("Seg " + i + ": Base and Limit must be valid integers.");
                return;
            }
        }

        // Validate
        List<String> errors = engine.validate(segments);
        if (!errors.isEmpty()) { errorLabel.setText(String.join("\n", errors)); return; }

        // Parse address
        String snStr = segNumField.getText().trim();
        String offStr = offsetField.getText().trim();
        if (snStr.isEmpty() || offStr.isEmpty()) {
            errorLabel.setText("Segment number and offset are required.");
            return;
        }
        int segNum, offset;
        try {
            segNum = Integer.parseInt(snStr);
            offset = Integer.parseInt(offStr);
        } catch (NumberFormatException e) {
            errorLabel.setText("Segment number and offset must be valid integers.");
            return;
        }

        // Translate
        session.setSimulationState(SimulationState.RUNNING);
        SegmentTranslationResult result = engine.translate(segments, segNum, offset);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        // Update stat cards
        targetSegValue.setText("Seg " + segNum);
        if (result.isValid()) {
            validationValue.setText("VALID");
            validationValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            physAddrValue.setText(String.valueOf(result.getPhysicalAddress()));
        } else {
            validationValue.setText("FAULT");
            validationValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            physAddrValue.setText("—");
        }

        // Draw memory map
        drawMemoryMap(segments);

        // Update validation log
        Label logEntry = new Label(result.getDescription());
        logEntry.setWrapText(true);
        logEntry.setMaxWidth(Double.MAX_VALUE);
        if (result.isValid()) {
            logEntry.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        } else {
            logEntry.setStyle("-fx-font-size: 13px; -fx-text-fill: #ef4444; -fx-font-weight: bold; " +
                    "-fx-padding: 8; -fx-background-color: derive(#ef4444, -85%); -fx-background-radius: 6;");
        }
        validationLog.getChildren().add(logEntry);
    }

    private void resetAll() {
        rebuildSegmentTable();
        segNumField.clear();
        offsetField.clear();
        validationLog.getChildren().clear();
        targetSegValue.setText("—");
        validationValue.setText("—");
        validationValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        physAddrValue.setText("—");
        drawEmptyMemoryMap();
        errorLabel.setText("");
        session.setSimulationState(SimulationState.IDLE);
    }
}
