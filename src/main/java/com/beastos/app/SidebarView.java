package com.beastos.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/* this is our sidebar navigation view! we designed this to sit on the left side
   of the app so the user can easily switch between all our OS modules (scheduling,
   memory, disk, etc.) and check system status at the bottom */
public class SidebarView {

    private final VBox view;
    private final NavigationService navService;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, Region> navIndicators = new LinkedHashMap<>();
    private final Map<String, Label> navLabels = new LinkedHashMap<>();
    private String activeItem = "";

    // we map each OS topic name to its corresponding icon file in the resources folder
    private static final String[][] MODULES = {
            {"CPU Scheduling",      "/icons/icon_cpu.png"},
            {"Banker's Algorithm",  "/icons/icon_banker.png"},
            {"Paging",              "/icons/icon_paging.png"},
            {"Page Replacement",    "/icons/icon_pagereplacement.png"},
            {"Segmentation",        "/icons/icon_segmentation.png"},
            {"Fragmentation",       "/icons/icon_fragmentation.png"},
            {"Disk Scheduling",     "/icons/icon_disk.png"},
            {"Multiprogramming",    "/icons/icon_multiprogramming.png"},
    };

    public SidebarView(NavigationService navService) {
        this.navService = navService;

        view = new VBox();
        view.getStyleClass().add("sidebar");

        // header with our BEAST OS branding logo
        HBox logoArea = createLogoArea();

        // workspace section with the overview/dashboard button
        Label workspaceLabel = createSectionLabel("WORKSPACE");
        Button overviewBtn = createNavButton("Overview", "/icons/icon_overview.png");
        navButtons.put("Overview", overviewBtn);

        // loop through all our OS simulation modules and create nav buttons for them
        Label modulesLabel = createSectionLabel("MODULES");
        VBox moduleButtons = new VBox(2);
        for (String[] mod : MODULES) {
            Button btn = createNavButton(mod[0], mod[1]);
            navButtons.put(mod[0], btn);
            moduleButtons.getChildren().add(btn);
        }

        // we use a growing spacer Region here so the status panel gets pushed all the way to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // status box at the bottom showing that our system is running okay
        VBox statusPanel = createStatusPanel();

        // assemble all components into the sidebar VBox in top-to-bottom order
        view.getChildren().addAll(
                logoArea,
                workspaceLabel, overviewBtn,
                modulesLabel, moduleButtons,
                spacer,
                statusPanel
        );
    }

    private HBox createLogoArea() {
        ImageView logoView = loadIcon("/icons/logo-full.png", 185);
        HBox logoArea;
        if (logoView != null) {
            logoArea = new HBox(logoView);
            logoArea.setAlignment(Pos.CENTER_LEFT);
            logoArea.setPadding(new Insets(16, 8, 12, 8));
        } else {
            // fallback text logo in case the image fails to load so the UI doesn't look empty lol
            Label beastLabel = new Label("BEAST");
            beastLabel.getStyleClass().add("sidebar-logo-text");
            Label osLabel = new Label(" OS");
            osLabel.getStyleClass().add("sidebar-logo-sub");
            HBox nameBox = new HBox(beastLabel, osLabel);
            nameBox.setAlignment(Pos.BASELINE_LEFT);
            logoArea = new HBox(10, nameBox);
            logoArea.setAlignment(Pos.CENTER_LEFT);
            logoArea.setPadding(new Insets(16, 12, 12, 12));
        }
        logoArea.getStyleClass().add("sidebar-logo-area");
        return logoArea;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sidebar-section-label");
        return label;
    }

    // basically this builds each nav button with an accent bar indicator, an icon, and the module name
    private Button createNavButton(String label, String iconPath) {
        Button btn = new Button();
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);

        // a thin vertical indicator bar on the left that lights up cyan when active
        Region indicator = new Region();
        indicator.getStyleClass().add("sidebar-indicator");
        indicator.setPrefSize(3, 18);
        indicator.setMinSize(3, 18);
        indicator.setMaxSize(3, 18);
        navIndicators.put(label, indicator);

        // load the module icon next to the indicator bar
        ImageView iconView = loadIcon(iconPath, 18);
        Node iconNode = (iconView != null) ? iconView : new Region();

        // the text label for the module button
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("sidebar-item-label");
        navLabels.put(label, textLabel);

        HBox content = new HBox(10, indicator, iconNode, textLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(4, 4, 4, 0));

        btn.setGraphic(content);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setOnAction(e -> navService.navigateTo(label));
        return btn;
    }

    // handles switching the active highlight styles when the user clicks a different section
    public void setActiveItem(String moduleName) {
        // clear the active styles from the previous selection
        if (!activeItem.isEmpty()) {
            if (navButtons.containsKey(activeItem)) {
                navButtons.get(activeItem).getStyleClass().remove("sidebar-item-active");
            }
            if (navIndicators.containsKey(activeItem)) {
                navIndicators.get(activeItem).getStyleClass().remove("sidebar-indicator-active");
            }
            if (navLabels.containsKey(activeItem)) {
                navLabels.get(activeItem).getStyleClass().remove("sidebar-item-label-active");
            }
        }

        // apply the active CSS classes to the newly selected item
        activeItem = moduleName;
        if (navButtons.containsKey(moduleName)) {
            Button btn = navButtons.get(moduleName);
            if (!btn.getStyleClass().contains("sidebar-item-active")) {
                btn.getStyleClass().add("sidebar-item-active");
            }
        }
        if (navIndicators.containsKey(moduleName)) {
            Region ind = navIndicators.get(moduleName);
            if (!ind.getStyleClass().contains("sidebar-indicator-active")) {
                ind.getStyleClass().add("sidebar-indicator-active");
            }
        }
        if (navLabels.containsKey(moduleName)) {
            Label lbl = navLabels.get(moduleName);
            if (!lbl.getStyleClass().contains("sidebar-item-label-active")) {
                lbl.getStyleClass().add("sidebar-item-label-active");
            }
        }
    }

    private VBox createStatusPanel() {
        Label title = new Label("LOCAL WORKSPACE");
        title.getStyleClass().add("sidebar-status-title");

        // little green status dot and text to show the system is up and running
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
        VBox.setMargin(panel, new Insets(8, 6, 16, 6));
        return panel;
    }

    // safe helper to load PNG icons — wrapped in try-catch so missing files won't crash our app
    private ImageView loadIcon(String path, double size) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                return iv;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public Node getView() {
        return view;
    }
}
