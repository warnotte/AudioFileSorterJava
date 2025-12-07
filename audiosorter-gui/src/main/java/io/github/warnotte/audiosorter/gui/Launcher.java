package io.github.warnotte.audiosorter.gui;

/**
 * Launcher class for fat JAR execution.
 * JavaFX requires a non-Application class as main entry point when running from a shaded JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
