package com.beastos.app;

/**
 * Standard launcher entry point that does not extend javafx.application.Application.
 * This bypasses the JavaFX runtime module check when running as a packaged standalone JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        BeastOSApp.main(args);
    }
}
