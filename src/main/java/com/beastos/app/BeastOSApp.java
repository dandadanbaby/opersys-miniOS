package com.beastos.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * BEAST OS — Interactive Operating System Workbench
 *
 * Main JavaFX application entry point.
 */
public class BeastOSApp extends Application {

    @Override
    public void start(Stage stage) {
        // Core services
        NavigationService navService = new NavigationService();

        // Layout: Sidebar (left, full height) | Right area (header + content)
        SidebarView sidebar = new SidebarView(navService);
        HeaderBar header = new HeaderBar();

        // Content area (where module views are swapped)
        VBox contentWrapper = new VBox();
        contentWrapper.getStyleClass().add("content-area");
        contentWrapper.setMinWidth(0);
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);

        // Right column: header on top, content below
        VBox rightColumn = new VBox(header.getView(), contentWrapper);
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        // Prevent the right column from computing an unbounded preferred width
        // which causes the stage/window to stretch to the right on startup
        rightColumn.setMinWidth(0);

        // Root container
        HBox root = new HBox(sidebar.getView(), rightColumn);
        root.getStyleClass().add("root");

        // Register the content wrapper with navigation service
        navService.setContentArea(contentWrapper);
        navService.setSidebar(sidebar);
        navService.setHeader(header);

        // Create scene
        Scene scene = new Scene(root, 1440, 850);
        scene.getStylesheets().add(
                getClass().getResource("/css/beast-os-theme.css").toExternalForm());

        // Stage setup
        stage.setScene(scene);
        stage.setTitle("BEAST OS — Interactive Operating System Workbench");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.setWidth(1440);
        stage.setHeight(850);

        // Store nav service in scene properties for cross-component access
        scene.getProperties().put("navService", navService);

        // Initialize session showcase defaults from overview-dashboard.png
        com.beastos.common.SimulationSession session = com.beastos.common.SimulationSession.getInstance();
        session.initDefaultProcesses(java.util.List.of("P1", "P2", "P3", "P4", "P5", "P6"));
        session.setCpuUtilization(72.4);
        session.setMemoryInUse(4.94); // 4.94 out of 8.0 GB = 61.8%
        session.setTotalMemory(8.0);
        session.setSimulationsCompleted(24);
        session.setResourceUtilization(38.0);
        session.setSimulationState(com.beastos.common.SimulationState.RUNNING);

        // Navigate to dashboard by default
        navService.navigateTo("Overview");

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
