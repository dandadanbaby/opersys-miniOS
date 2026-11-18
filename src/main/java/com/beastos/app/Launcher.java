package com.beastos.app;

// we had to create this separate launcher class because JavaFX gets mad if the main class
// extends Application when you package everything into an executable JAR.
// basically, this class doesn't extend Application, so it bypasses the JavaFX module check
// and lets us boot up BeastOSApp cleanly without runtime errors!
public class Launcher {
    public static void main(String[] args) {
        BeastOSApp.main(args);
    }
}
