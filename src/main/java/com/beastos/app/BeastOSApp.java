package com.beastos.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/* BEAST OS — our group project for operating systems class!
 * This is the main entry point where our JavaFX app kicks off.
 * Basically what happens here is we set up the whole window layout,
 * link our navigation system, and pre-load some sample OS stats so
 * the dashboard looks alive right when it opens.
 */
public class BeastOSApp extends Application {

    @Override
    public void start(Stage stage) {
        // this is our navigation manager that handles switching between OS modules
        NavigationService navService = new NavigationService();

        // our layout split: sidebar on the left, header + content view on the right
        SidebarView sidebar = new SidebarView(navService);
        HeaderBar header = new HeaderBar();

        // this is where we swap out the active view (CPU scheduling, memory management, etc.)
        VBox contentWrapper = new VBox();
        contentWrapper.getStyleClass().add("content-area");
        contentWrapper.setMinWidth(0);
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);

        // stack the header bar directly above whatever simulation view the user has open
        VBox rightColumn = new VBox(header.getView(), contentWrapper);
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        // tricky bug we ran into: without minWidth(0), JavaFX kept expanding this column past the screen bounds on startup
        rightColumn.setMinWidth(0);

        // root container putting the sidebar and main area side-by-side
        HBox root = new HBox(sidebar.getView(), rightColumn);
        root.getStyleClass().add("root");

        // we connect these so navigation buttons know where to swap in the views and update the header
        navService.setContentArea(contentWrapper);
        navService.setSidebar(sidebar);
        navService.setHeader(header);

        // set up the scene with our custom dark theme styling
        Scene scene = new Scene(root, 1440, 850);
        scene.getStylesheets().add(
                getClass().getResource("/css/beast-os-theme.css").toExternalForm());

        // window bounds — we set 1024x700 minimum so Gantt charts and memory tables don't get squished
        stage.setScene(scene);
        stage.setTitle("BEAST OS — Interactive Operating System Workbench");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.setWidth(1440);
        stage.setHeight(850);

        // we stash navService in scene properties so any child component can grab it without prop-drilling
        scene.getProperties().put("navService", navService);

        // load initial showcase metrics so the dashboard isn't completely empty when launching
        com.beastos.common.SimulationSession session = com.beastos.common.SimulationSession.getInstance();
        session.initDefaultProcesses(java.util.List.of("P1", "P2", "P3", "P4", "P5", "P6"));
        session.setCpuUtilization(72.4);
        session.setMemoryInUse(4.94); // 4.94 GB out of 8.0 GB is about 61.8% RAM used
        session.setTotalMemory(8.0);
        session.setSimulationsCompleted(24);
        session.setResourceUtilization(38.0);
        session.setSimulationState(com.beastos.common.SimulationState.RUNNING);

        // start on the overview dashboard by default
        navService.navigateTo("Overview");

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
