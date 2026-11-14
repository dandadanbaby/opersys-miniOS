package com.beastos.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Left sidebar containing BEAST OS branding, workspace navigation,
 * module list, and status panel.
 */
public class SidebarView {

    private final VBox view;
    private final NavigationService navService;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private String activeItem = "";

    // Module icons (Unicode)
    private static final String[][] MODULES = {
            {"CPU Scheduling",      "\uD83D\uDDA5"},  // 🖥
            {"Banker's Algorithm",  "\uD83D\uDC64"},  // 👤
            {"Paging",              "\uD83D\uDCC4"},  // 📄
            {"Page Replacement",    "\uD83D\uDCC3"},  // 📃
            {"Segmentation",        "\uD83D\uDDC2"},  // 🗂
            {"Fragmentation",       "\uD83E\uDDE9"},  // 🧩
            {"Disk Scheduling",     "\uD83D\uDCBF"},  // 💿
            {"Multiprogramming",    "⚙"},             // ⚙
    };

    public SidebarView(NavigationService navService) {
        this.navService = navService;

        view = new VBox();
        view.getStyleClass().add("sidebar");

        // --- Logo Area ---
        HBox logoArea = createLogoArea();

        // --- Workspace Section ---
        Label workspaceLabel = createSectionLabel("WORKSPACE");
        Button overviewBtn = createNavButton("Overview", "\uD83D\uDCCA"); // 📊
        navButtons.put("Overview", overviewBtn);

        // --- Modules Section ---
        Label modulesLabel = createSectionLabel("MODULES");
        VBox moduleButtons = new VBox();
        for (String[] mod : MODULES) {
            Button btn = createNavButton(mod[0], mod[1]);
            navButtons.put(mod[0], btn);
            moduleButtons.getChildren().add(btn);
        }

        // --- Spacer ---
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- Status Panel ---
        VBox statusPanel = createStatusPanel();

        // --- Assemble ---
        view.getChildren().addAll(
                logoArea,
                workspaceLabel, overviewBtn,
                modulesLabel, moduleButtons,
                spacer,
                statusPanel
        );
    }

    private HBox createLogoArea() {
        // Dragon icon placeholder
        Label iconLabel = new Label("\uD83D\uDC32"); // 🐲
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label beastLabel = new Label("BEAST");
        beastLabel.getStyleClass().add("sidebar-logo-text");

        Label osLabel = new Label(" OS");
        osLabel.getStyleClass().add("sidebar-logo-sub");

        HBox nameBox = new HBox(beastLabel, osLabel);
        nameBox.setAlignment(Pos.BASELINE_LEFT);

        VBox textBox = new VBox(nameBox);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox logoArea = new HBox(10, iconLabel, textBox);
        logoArea.getStyleClass().add("sidebar-logo-area");
        logoArea.setAlignment(Pos.CENTER_LEFT);
        return logoArea;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sidebar-section-label");
        return label;
    }

    private Button createNavButton(String label, String icon) {
        Button btn = new Button(icon + "   " + label);
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navService.navigateTo(label));
        return btn;
    }

    /** Highlight the active navigation item. */
    public void setActiveItem(String moduleName) {
        // Remove active from previous
        if (!activeItem.isEmpty() && navButtons.containsKey(activeItem)) {
            navButtons.get(activeItem).getStyleClass().remove("sidebar-item-active");
        }
        // Set new active
        activeItem = moduleName;
        if (navButtons.containsKey(moduleName)) {
            Button btn = navButtons.get(moduleName);
            if (!btn.getStyleClass().contains("sidebar-item-active")) {
                btn.getStyleClass().add("sidebar-item-active");
            }
        }
    }

    private VBox createStatusPanel() {
        Label title = new Label("LOCAL WORKSPACE");
        title.getStyleClass().add("sidebar-status-title");

        // Green dot + status text
        Region dot = new Region();
        dot.getStyleClass().add("sidebar-status-dot");

        Label statusText = new Label("All systems operational");
        statusText.getStyleClass().add("sidebar-status-text");

        HBox statusRow = new HBox(6, dot, statusText);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Label version = new Label("BEAST OS v1.0.0");
        version.getStyleClass().add("sidebar-version");

        VBox panel = new VBox(4, title, statusRow, version);
        panel.getStyleClass().add("sidebar-status-panel");
        VBox.setMargin(panel, new Insets(8, 12, 16, 12));
        return panel;
    }

    public Node getView() {
        return view;
    }
}
