package io.github.warnotte.audiosorter.cli;

import io.github.warnotte.audiosorter.core.AudioSorterEngine;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.listener.ConsoleProgressListener;
import io.github.warnotte.audiosorter.model.RunTotals;
import io.github.warnotte.audiosorter.report.HtmlReportGenerator;
import io.github.warnotte.audiosorter.report.JsonReportGenerator;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CLI entry point for the Audio File Sorter.
 */
public class Main {

    // Configuration - modify these values or use command line arguments
	//private static final String INPUT_DIRECTORY = "e:\\manson";
	private static final String INPUT_DIRECTORY = "d:\\mp3";
	private static final String OUTPUT_DIRECTORY = "e:\\manson_sorted";
	private static final boolean DEBUG_MODE = true;

    private static final Path REPORTS_DIR = Path.of("reports");

    public static void main(String[] args) throws Exception {
        // Clean up directories
        deleteDir(new File("logs"));
        deleteDir(REPORTS_DIR.toFile());
        Files.createDirectories(REPORTS_DIR);

        // Parse command line arguments or use defaults
        String inputDir = args.length > 0 ? args[0] : INPUT_DIRECTORY;
        String outputDir = args.length > 1 ? args[1] : OUTPUT_DIRECTORY;
        boolean debugMode = args.length > 2 ? Boolean.parseBoolean(args[2]) : DEBUG_MODE;

        // Create configuration
        SortConfiguration config = new SortConfiguration(
            Path.of(inputDir),
            Path.of(outputDir)
        );
        config.setDebugMode(debugMode);

        // Create engine and add console listener
        AudioSorterEngine engine = new AudioSorterEngine(config);
        engine.addListener(new ConsoleProgressListener());

        // Execute
        RunTotals totals = engine.execute();

        // Generate reports
        generateReports(totals, config);

        // Open HTML report in browser
        openReport(REPORTS_DIR.resolve("report.html").toFile());
    }

    private static void generateReports(RunTotals totals, SortConfiguration config) {
        // Generate HTML report
        try {
            HtmlReportGenerator htmlGenerator = new HtmlReportGenerator();
            Path htmlReport = REPORTS_DIR.resolve("report.html");
            htmlGenerator.generate(totals, config, htmlReport);
            System.out.println("Generated HTML report: " + htmlReport.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to generate HTML report: " + e.getMessage());
            e.printStackTrace();
        }

        // Generate JSON report
        try {
            JsonReportGenerator jsonGenerator = new JsonReportGenerator();
            Path jsonReport = REPORTS_DIR.resolve("report.json");
            jsonGenerator.generate(totals, config, jsonReport);
            System.out.println("Generated JSON report: " + jsonReport.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to generate JSON report: " + e.getMessage());
        }
    }

    private static void openReport(File reportFile) {
        if (Desktop.isDesktopSupported() && reportFile.exists()) {
            try {
                Desktop.getDesktop().open(reportFile);
            } catch (IOException e) {
                System.err.println("Could not open report file: " + e.getMessage());
            }
        }
    }

    private static void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}
