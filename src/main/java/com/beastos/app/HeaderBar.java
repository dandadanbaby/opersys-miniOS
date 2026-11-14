package com.beastos.app;

import com.beastos.common.SimulationSession;
import com.beastos.common.SimulationState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Top header bar displaying breadcrumb, system time, and simulation status.
 */
public class HeaderBar {

    private final HBox view;
    private final Label breadcrumbPrefix;
    private final Label breadcrumbCurrent;
    private final Label timeValue;
    private final Label statusValue;
    private final NavigationService navService;
    private final SimulationSession session = SimulationSession.getInstance();

    public HeaderBar(NavigationService navService) {
        this.navService = navService;

        // Breadcrumb
        breadcrumbPrefix = new Label("WORKSPACE  /  ");
        breadcrumbPrefix.getStyleClass().add("header-breadcrumb");

        breadcrumbCurrent = new Label("Overview");
        breadcrumbCurrent.getStyleClass().add("header-breadcrumb-current");

        HBox breadcrumb = new HBox(breadcrumbPrefix, breadcrumbCurrent);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // System Time pill
        Label timeLabel = new Label("System Time:");
        timeLabel.getStyleClass().add("header-pill-label");
        timeValue = new Label("00:00:00");
        timeValue.getStyleClass().add("header-pill-value");
        HBox timePill = new HBox(6, timeLabel, timeValue);
        timePill.getStyleClass().add("header-pill");
        timePill.setAlignment(Pos.CENTER);

        // Start clock
        startClock();

        // Simulation Status pill
        Label statusLabel = new Label("OS Simulation Status :");
        statusLabel.getStyleClass().add("header-pill-label");
        statusValue = new Label("IDLE");
        statusValue.getStyleClass().add("header-pill-value");
        HBox statusPill = new HBox(6, statusLabel, statusValue);
        statusPill.getStyleClass().add("header-status-pill");
        statusPill.setAlignment(Pos.CENTER);

        // Bind status to session
        session.simulationStateProperty().addListener((obs, oldVal, newVal) -> {
            statusValue.setText(newVal.getDisplayName());
        });

        // Assemble header
        view = new HBox(16, breadcrumb, spacer, timePill, statusPill);
        view.getStyleClass().add("header-bar");
        view.setAlignment(Pos.CENTER);
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeValue.setText(LocalTime.now().format(formatter));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        // Set initial value
        timeValue.setText(LocalTime.now().format(formatter));
    }

    /** Update the breadcrumb to reflect current navigation. */
    public void updateBreadcrumb(String currentView) {
        breadcrumbCurrent.setText(currentView);
    }

    public Node getView() {
        return view;
    }
}
