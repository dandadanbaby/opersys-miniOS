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
    private final SimulationSession session = SimulationSession.getInstance();

    public HeaderBar() {
        // Breadcrumb
        breadcrumbPrefix = new Label("WORKSPACE  /  ");
        breadcrumbPrefix.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: bold;");

        breadcrumbCurrent = new Label("Overview");
        breadcrumbCurrent.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox breadcrumb = new HBox(breadcrumbPrefix, breadcrumbCurrent);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // System Time pill
        Label timeLabel = new Label("System Time:");
        timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        timeValue = new Label("00:00:00");
        timeValue.setStyle("-fx-text-fill: #2dd4bf; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Consolas', 'Courier New', monospace;");
        HBox timePill = new HBox(6, timeLabel, timeValue);
        timePill.getStyleClass().add("header-pill");
        timePill.setAlignment(Pos.CENTER);

        // Start clock
        startClock();

        // Simulation Status pill
        Region greenDot = new Region();
        greenDot.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 4; -fx-min-width: 7; -fx-min-height: 7; -fx-max-width: 7; -fx-max-height: 7;");
        Label statusLabel = new Label("OS Simulation Status :");
        statusLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        statusValue = new Label("ACTIVE");
        statusValue.setStyle("-fx-text-fill: #2dd4bf; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox statusPill = new HBox(6, greenDot, statusLabel, statusValue);
        statusPill.getStyleClass().add("header-pill");
        statusPill.setAlignment(Pos.CENTER);

        // Bind status to session
        session.simulationStateProperty().addListener((obs, oldVal, newVal) -> {
            String name = newVal.getDisplayName();
            statusValue.setText(name.equals("RUNNING") ? "ACTIVE" : name);
            if (newVal == SimulationState.IDLE) {
                greenDot.setStyle("-fx-background-color: #64748b; -fx-background-radius: 4; -fx-min-width: 7; -fx-min-height: 7; -fx-max-width: 7; -fx-max-height: 7;");
            } else {
                greenDot.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 4; -fx-min-width: 7; -fx-min-height: 7; -fx-max-width: 7; -fx-max-height: 7;");
            }
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
