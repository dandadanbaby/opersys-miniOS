package com.beastos.fragmentation.view;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import com.beastos.fragmentation.engine.FragmentationEngine;
import com.beastos.fragmentation.engine.FragmentationEngine.Strategy;
import com.beastos.fragmentation.model.AllocationResult;
import com.beastos.fragmentation.model.FragmentationOutput;
import com.beastos.fragmentation.model.MemoryBlock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmentation module view for BEAST OS.
 * Matches design: allocation table with algorithm tabs, memory block partition view,
 * and fragmentation analysis summary.
 */
public class FragmentationView extends ScrollPane {

    private static final SimulationSession session = SimulationSession.getInstance();
    private static final FragmentationEngine engine = new FragmentationEngine();

    // Algorithm selection
    private Strategy selectedStrategy = Strategy.FIRST_FIT;
    private final List<Button> algoTabs = new ArrayList<>();

    // Input
    private final VBox blockInputRows = new VBox(4);
    private final VBox processInputRows = new VBox(4);
    private int blockCounter = 0;
    private int processCounter = 0;

    // Stat cards
    private final Label memBlocksValue = new Label("00");
    private final Label allocatedValue = new Label("00");
    private final Label allocatedSub = new Label(" ");
    private final Label intFragValue = new Label("0 KB");
    private final Label extFragValue = new Label("0 KB");

    // Results
    private final TableView<AllocationResult> allocationTable = new TableView<>();
    private final VBox partitionView = new VBox(6);
    private final VBox analysisLog = new VBox(6);
    private final VBox analysisSummaryCards = new VBox(8);
    private final Label errorLabel = new Label();

    public FragmentationView() {
        VBox content = new VBox(20);

        content.getChildren().add(createHeader());
        content.getChildren().add(createStatCards());
        content.getChildren().add(createInputSection());
        content.getChildren().add(createMainArea());
        content.getChildren().add(createAnalysisPanel());

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Default blocks and processes
        for (int i = 0; i < 5; i++) addBlockRow();
        for (int i = 0; i < 6; i++) addProcessRow();
    }

    private VBox createHeader() {
        Label tag = new Label("OPERATING SYSTEM WORKBENCH");
        tag.getStyleClass().add("content-section-tag");
        Label title = new Label("Fragmentation");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("COMMAND CENTER");
        subtitle.getStyleClass().add("content-subtitle");
        Label desc = new Label("Simulate memory allocation and visualize internal or external fragmentation using dynamic workload datasets.");
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
        VBox c1 = buildStatCard("MEMORY BLOCKS", "🧱", memBlocksValue, " ");
        VBox c2 = buildStatCard("ALLOCATED PROCESSES", "👤", allocatedValue, " ");
        allocatedSub.getStyleClass().add("stat-card-description");
        VBox c3 = buildStatCard("TOTAL INTERNAL FRAG", "■", intFragValue, " ");
        intFragValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        VBox c4 = buildStatCard("EXTERNAL FRAG", "□", extFragValue, " ");

        HBox row = new HBox(14, c1, c2, c3, c4);
        row.setAlignment(Pos.TOP_LEFT);
        for (VBox c : new VBox[]{c1, c2, c3, c4}) HBox.setHgrow(c, Priority.ALWAYS);
        return row;
    }

    private VBox buildStatCard(String label, String icon, Label value, String descText) {
        Label l = new Label(label);
        l.getStyleClass().add("stat-card-label");
        Label ic = new Label(icon);
        ic.getStyleClass().add("stat-card-icon");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox top = new HBox(l, sp, ic);
        top.setAlignment(Pos.CENTER);
        value.getStyleClass().add("stat-card-value");
        Label d = new Label(descText);
        d.getStyleClass().add("stat-card-description");
        VBox card = new VBox(4, top, value, d);
        card.getStyleClass().add("stat-card");
        return card;
    }

    // ----------------------------------------------------------------
    //  Input Section — Blocks + Processes + Algorithm tabs
    // ----------------------------------------------------------------
    private VBox createInputSection() {
        // Algorithm tabs
        HBox tabBar = createAlgorithmTabs();

        // Two-column: blocks on left, processes on right
        VBox blockPanel = createBlockInput();
        VBox processPanel = createProcessInput();

        HBox.setHgrow(blockPanel, Priority.ALWAYS);
        HBox.setHgrow(processPanel, Priority.ALWAYS);
        HBox inputCols = new HBox(14, blockPanel, processPanel);

        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        Button runBtn = new Button("Allocate Memory");
        runBtn.getStyleClass().add("btn-accent");
        runBtn.setOnAction(e -> runAllocation());

        VBox section = new VBox(10, tabBar, inputCols, errorLabel, runBtn);
        section.getStyleClass().add("panel");
        return section;
    }

    private HBox createAlgorithmTabs() {
        String[] strategies = {"First-Fit", "Best-Fit", "Worst-Fit"};
        HBox tabBar = new HBox(8);
        tabBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Memory Allocation Table");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 18px;");
        tabBar.getChildren().add(title);

        for (String s : strategies) {
            Button tab = new Button(s);
            tab.getStyleClass().add("algo-tab");
            if (s.equals("First-Fit")) tab.getStyleClass().add("algo-tab-active");
            tab.setOnAction(e -> selectStrategy(s));
            algoTabs.add(tab);
            tabBar.getChildren().add(tab);
        }
        return tabBar;
    }

    private void selectStrategy(String name) {
        selectedStrategy = switch (name) {
            case "Best-Fit" -> Strategy.BEST_FIT;
            case "Worst-Fit" -> Strategy.WORST_FIT;
            default -> Strategy.FIRST_FIT;
        };
        for (Button tab : algoTabs) {
            tab.getStyleClass().remove("algo-tab-active");
            if (tab.getText().equals(name)) tab.getStyleClass().add("algo-tab-active");
        }
    }

    private VBox createBlockInput() {
        Label title = new Label("Memory Blocks (KB)");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Button addBtn = new Button("+ Block");
        addBtn.getStyleClass().add("btn-outline");
        addBtn.setOnAction(e -> addBlockRow());
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox header = new HBox(8, title, sp, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(blockInputRows);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(150);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return new VBox(6, header, scroll);
    }

    private VBox createProcessInput() {
        Label title = new Label("Processes (KB)");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        Button addBtn = new Button("+ Process");
        addBtn.getStyleClass().add("btn-outline");
        addBtn.setOnAction(e -> addProcessRow());
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox header = new HBox(8, title, sp, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(processInputRows);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(150);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return new VBox(6, header, scroll);
    }

    private void addBlockRow() {
        blockCounter++;
        Label lbl = new Label("Block " + blockCounter + ":");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        lbl.setPrefWidth(70);
        TextField field = new TextField();
        field.setPromptText("Size KB");
        field.setPrefWidth(90);
        Button del = new Button("✕");
        del.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-padding: 2 6;");
        HBox row = new HBox(6, lbl, field, del);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(field);
        del.setOnAction(e -> { if (blockInputRows.getChildren().size() > 1) blockInputRows.getChildren().remove(row); });
        blockInputRows.getChildren().add(row);
    }

    private void addProcessRow() {
        processCounter++;
        Label lbl = new Label("P" + processCounter + ":");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #2dd4bf; -fx-font-weight: bold;");
        lbl.setPrefWidth(40);
        TextField field = new TextField();
        field.setPromptText("Size KB");
        field.setPrefWidth(90);
        Button del = new Button("✕");
        del.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-padding: 2 6;");
        HBox row = new HBox(6, lbl, field, del);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(new Object[]{lbl, field});
        del.setOnAction(e -> { if (processInputRows.getChildren().size() > 1) processInputRows.getChildren().remove(row); });
        processInputRows.getChildren().add(row);
    }

    // ----------------------------------------------------------------
    //  Main Area — Allocation Table + Partition View
    // ----------------------------------------------------------------
    private HBox createMainArea() {
        VBox leftPanel = createAllocationTablePanel();
        VBox rightPanel = createPartitionPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        return new HBox(14, leftPanel, rightPanel);
    }

    @SuppressWarnings("unchecked")
    private VBox createAllocationTablePanel() {
        // Table columns
        TableColumn<AllocationResult, String> pidCol = new TableColumn<>("Process ID");
        pidCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getProcessId()));
        pidCol.setPrefWidth(70);

        TableColumn<AllocationResult, String> sizeCol = new TableColumn<>("Process Size");
        sizeCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getProcessSize() + " KB"));
        sizeCol.setPrefWidth(85);

        TableColumn<AllocationResult, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatus()));
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText("● " + item);
                    if ("Allocated".equals(item)) setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<AllocationResult, String> blockCol = new TableColumn<>("Block Used");
        blockCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getBlockDisplay()));
        blockCol.setPrefWidth(120);

        TableColumn<AllocationResult, String> fragCol = new TableColumn<>("Internal Frag");
        fragCol.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().isAllocated() ? r.getValue().getInternalFrag() + " KB" : "N/A"));
        fragCol.setPrefWidth(85);

        allocationTable.getColumns().addAll(pidCol, sizeCol, statusCol, blockCol, fragCol);
        allocationTable.setPlaceholder(new Label("Run allocation to see results"));
        allocationTable.setPrefHeight(220);
        allocationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        pidCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-text-fill: #2dd4bf; -fx-font-weight: bold;"); }
            }
        });

        VBox panel = new VBox(8, allocationTable);
        panel.getStyleClass().add("panel");
        return panel;
    }

    private VBox createPartitionPanel() {
        Label title = new Label("Proportional Memory Block Partition");
        title.getStyleClass().add("panel-title");
        title.setStyle("-fx-font-size: 16px;");

        // Legend
        Label legend = new Label("● Allocated Space (Teal)   ● Internal Frag (Amber)   ● Free Blocks (Steel Blue)");
        legend.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        ScrollPane scroll = new ScrollPane(partitionView);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox panel = new VBox(8, title, legend, scroll);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // ----------------------------------------------------------------
    //  Analysis Panel
    // ----------------------------------------------------------------
    private VBox createAnalysisPanel() {
        Label title = new Label("Fragmentation Analysis Summary");
        title.getStyleClass().add("panel-title");
        Label sub = new Label("ANALYSIS LOG");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        ScrollPane logScroll = new ScrollPane(analysisLog);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(100);
        logScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox bottom = new HBox(14, logScroll, analysisSummaryCards);
        HBox.setHgrow(logScroll, Priority.ALWAYS);
        analysisSummaryCards.setMinWidth(220);

        VBox panel = new VBox(8, title, sub, bottom);
        panel.getStyleClass().add("panel");
        return panel;
    }

    // ----------------------------------------------------------------
    //  Simulation
    // ----------------------------------------------------------------
    private void runAllocation() {
        errorLabel.setText("");

        // Parse blocks
        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < blockInputRows.getChildren().size(); i++) {
            HBox row = (HBox) blockInputRows.getChildren().get(i);
            TextField field = (TextField) row.getUserData();
            String text = field.getText().trim();
            if (text.isEmpty()) { errorLabel.setText("Block " + (i + 1) + " size is required."); return; }
            try {
                int size = Integer.parseInt(text);
                if (size <= 0) { errorLabel.setText("Block " + (i + 1) + " must have positive size."); return; }
                blocks.add(new MemoryBlock(i + 1, size));
            } catch (NumberFormatException e) {
                errorLabel.setText("Block " + (i + 1) + ": invalid size."); return;
            }
        }

        // Parse processes
        List<String> pids = new ArrayList<>();
        List<Integer> pSizes = new ArrayList<>();
        for (int i = 0; i < processInputRows.getChildren().size(); i++) {
            HBox row = (HBox) processInputRows.getChildren().get(i);
            Object[] data = (Object[]) row.getUserData();
            Label lbl = (Label) data[0];
            TextField field = (TextField) data[1];
            String text = field.getText().trim();
            if (text.isEmpty()) { errorLabel.setText(lbl.getText() + " size is required."); return; }
            try {
                int size = Integer.parseInt(text);
                if (size <= 0) { errorLabel.setText(lbl.getText() + " must have positive size."); return; }
                pids.add(lbl.getText().replace(":", ""));
                pSizes.add(size);
            } catch (NumberFormatException e) {
                errorLabel.setText(lbl.getText() + " invalid size."); return;
            }
        }

        if (blocks.isEmpty() || pids.isEmpty()) {
            errorLabel.setText("At least one block and one process are required."); return;
        }

        // Run allocation
        session.setSimulationState(SimulationState.RUNNING);
        int[] sizes = pSizes.stream().mapToInt(Integer::intValue).toArray();
        FragmentationOutput output = engine.allocate(blocks, pids.toArray(new String[0]), sizes, selectedStrategy);
        session.setSimulationState(SimulationState.COMPLETED);
        session.incrementSimulationsCompleted();

        // Update stat cards
        memBlocksValue.setText(String.format("%02d", blocks.size()));
        allocatedValue.setText(String.format("%02d / %02d", output.getAllocatedCount(), output.getTotalProcesses()));
        if (output.getWaitingCount() > 0) {
            allocatedSub.setText(String.format("%02d in queue", output.getWaitingCount()));
        } else {
            allocatedSub.setText("All allocated");
        }
        intFragValue.setText(output.getTotalInternalFrag() + " KB");
        intFragValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        extFragValue.setText(output.getExternalFrag() + " KB");

        // Update allocation table
        allocationTable.setItems(FXCollections.observableArrayList(output.getResults()));

        // Update partition view
        buildPartitionView(output, blocks);

        // Update analysis log
        buildAnalysisLog(output);
    }

    private void buildPartitionView(FragmentationOutput output, List<MemoryBlock> blocks) {
        partitionView.getChildren().clear();

        for (MemoryBlock block : blocks) {
            // Find which process was allocated to this block
            AllocationResult allocated = null;
            for (AllocationResult r : output.getResults()) {
                if (r.isAllocated() && r.getBlockId() == block.getBlockId()) {
                    allocated = r;
                    break;
                }
            }

            HBox row = new HBox(4);
            row.setAlignment(Pos.CENTER_LEFT);

            Label blockLabel = new Label("Block " + block.getBlockId() + " (" + block.getSize() + " KB)");
            blockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-min-width: 120;");
            row.getChildren().add(blockLabel);

            if (allocated != null) {
                // Process block (teal)
                Label procBar = new Label(allocated.getProcessId() + " (" + allocated.getProcessSize() + " KB)");
                procBar.setStyle("-fx-background-color: #14716a; -fx-background-radius: 4; " +
                        "-fx-text-fill: #f8fafc; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8;");
                row.getChildren().add(procBar);

                if (allocated.getInternalFrag() > 0) {
                    Label fragBar = new Label("Internal Frag (" + allocated.getInternalFrag() + " KB)");
                    fragBar.setStyle("-fx-background-color: #92400e; -fx-background-radius: 4; " +
                            "-fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-padding: 4 8;");
                    row.getChildren().add(fragBar);
                }
            } else {
                Label freeBar = new Label("Unallocated Free Space (" + block.getSize() + " KB)");
                freeBar.setStyle("-fx-background-color: #1e3a5f; -fx-background-radius: 4; " +
                        "-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-padding: 4 8;");
                row.getChildren().add(freeBar);
            }

            partitionView.getChildren().add(row);
        }

        // External frag row
        if (output.getExternalFrag() > 0) {
            Label extLabel = new Label("External Frag");
            extLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-min-width: 120;");
            Label extBar = new Label("Unallocated Free Space (" + output.getExternalFrag() + " KB)");
            extBar.setStyle("-fx-background-color: #1e3a5f; -fx-background-radius: 4; " +
                    "-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-padding: 4 8;");
            HBox extRow = new HBox(4, extLabel, extBar);
            extRow.setAlignment(Pos.CENTER_LEFT);
            partitionView.getChildren().add(extRow);
        }
    }

    private void buildAnalysisLog(FragmentationOutput output) {
        analysisLog.getChildren().clear();
        analysisSummaryCards.getChildren().clear();

        Label success = new Label("[SUCCESS] Memory Blocks correctly validated with dynamic processes.");
        success.setStyle("-fx-font-size: 12px; -fx-text-fill: #22c55e; " +
                "-fx-font-family: 'Consolas', monospace;");
        success.setWrapText(true);

        Label info = new Label("[INFO] Total Internal Fragmentation is " + output.getTotalInternalFrag()
                + " KB (scattered across " + output.getAllocatedCount() + " active blocks).");
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b82f6; -fx-font-family: 'Consolas', monospace;");
        info.setWrapText(true);

        analysisLog.getChildren().addAll(success, info);

        if (output.getExternalFrag() > 0) {
            Label alert = new Label("[ALERT] External Fragmentation detected: " + output.getExternalFrag()
                    + " KB of unallocated fragmented free memory is available across free memory space"
                    + (output.getWaitingCount() > 0 ? ", but " + output.getWaitingCount() + " process(es) remain waiting." : "."));
            alert.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b; -fx-font-family: 'Consolas', monospace;");
            alert.setWrapText(true);
            analysisLog.getChildren().add(alert);
        }

        // Summary cards
        int totalBlockSize = 0;
        for (int i = 0; i < blockInputRows.getChildren().size(); i++) {
            HBox row = (HBox) blockInputRows.getChildren().get(i);
            TextField f = (TextField) row.getUserData();
            try { totalBlockSize += Integer.parseInt(f.getText().trim()); } catch (Exception ignored) {}
        }
        int totalProcessSize = 0;
        for (AllocationResult r : output.getResults()) {
            if (r.isAllocated()) totalProcessSize += r.getProcessSize();
        }
        double allocPct = totalBlockSize > 0 ? (double) totalProcessSize / totalBlockSize * 100 : 0;

        analysisSummaryCards.getChildren().addAll(
                summaryCard(String.format("%.1f%% Allocated Space", allocPct)),
                summaryCard("Internal Frag: " + output.getTotalInternalFrag() + " KB"),
                summaryCard("External Frag: " + output.getExternalFrag() + " KB")
        );
    }

    private Label summaryCard(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc; " +
                "-fx-padding: 10; -fx-background-color: #1c2333; -fx-background-radius: 8; " +
                "-fx-border-color: #334155; -fx-border-radius: 8; -fx-min-width: 200; -fx-alignment: center;");
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void resetAll() {
        blockInputRows.getChildren().clear();
        processInputRows.getChildren().clear();
        blockCounter = 0;
        processCounter = 0;
        for (int i = 0; i < 5; i++) addBlockRow();
        for (int i = 0; i < 6; i++) addProcessRow();
        allocationTable.getItems().clear();
        partitionView.getChildren().clear();
        analysisLog.getChildren().clear();
        analysisSummaryCards.getChildren().clear();
        memBlocksValue.setText("00");
        allocatedValue.setText("00");
        intFragValue.setText("0 KB");
        extFragValue.setText("0 KB");
        errorLabel.setText("");
        session.setSimulationState(SimulationState.IDLE);
    }
}
