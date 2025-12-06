package io.github.warnotte.audiosorter.cli;

import io.github.warnotte.audiosorter.core.AudioSorterEngine;
import io.github.warnotte.audiosorter.core.MusicScanner;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.listener.ConsoleProgressListener;
import io.github.warnotte.audiosorter.listener.ConsoleScanListener;
import io.github.warnotte.audiosorter.model.RunTotals;
import io.github.warnotte.audiosorter.report.HtmlReportGenerator;
import io.github.warnotte.audiosorter.report.JsonReportGenerator;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * CLI entry point for the Audio File Sorter using picocli.
 */
@Command(
    name = "audiosorter",
    mixinStandardHelpOptions = true,
    version = "AudioFilesSorter 0.1.0",
    description = "Music collection analyzer and organizer",
    subcommands = {
        Main.ScanCommand.class,
        Main.SortCommand.class
    }
)
public class Main implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // No subcommand specified - show help
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Scan command - analyze directory and generate reports without copying files.
     */
    @Command(
        name = "scan",
        mixinStandardHelpOptions = true,
        description = "Scan a music directory and generate catalog/reports (no files are copied)"
    )
    static class ScanCommand implements Callable<Integer> {

        @Parameters(
            index = "0",
            description = "Input directory to scan"
        )
        private Path inputDir;

        @Option(
            names = {"-o", "--output"},
            description = "Output directory for reports (default: ./reports)"
        )
        private Path reportsDir = Path.of("reports");

        @Option(
            names = {"--no-open"},
            description = "Don't open the report in browser after generation"
        )
        private boolean noOpen = false;

        @Override
        public Integer call() throws Exception {
            System.out.println("=== SCAN MODE ===");
            System.out.println("Input: " + inputDir);
            System.out.println();

            // Clean up and create reports directory
            deleteDir(reportsDir.toFile());
            Files.createDirectories(reportsDir);

            // Create scanner
            MusicScanner scanner = new MusicScanner(inputDir);
            scanner.addListener(new ConsoleScanListener());

            // Execute scan
            RunTotals totals = scanner.scan();

            // Generate reports
            SortConfiguration config = new SortConfiguration();
            config.setInputDirectory(inputDir);
            config.setOutputDirectory(inputDir);
            config.setDebugMode(true);

            generateReports(totals, config, reportsDir);

            if (!noOpen) {
                openReport(reportsDir.resolve("report.html").toFile());
            }

            return 0;
        }
    }

    /**
     * Sort command - scan and copy files to output directory.
     */
    @Command(
        name = "sort",
        mixinStandardHelpOptions = true,
        description = "Scan and copy/organize files to output directory (Artist/[Year] Album structure)"
    )
    static class SortCommand implements Callable<Integer> {

        @Parameters(
            index = "0",
            description = "Input directory containing music files"
        )
        private Path inputDir;

        @Parameters(
            index = "1",
            description = "Output directory for organized files"
        )
        private Path outputDir;

        @Option(
            names = {"-r", "--reports"},
            description = "Output directory for reports (default: ./reports)"
        )
        private Path reportsDir = Path.of("reports");

        @Option(
            names = {"--dry-run"},
            description = "Scan only, don't actually copy files"
        )
        private boolean dryRun = false;

        @Option(
            names = {"--no-open"},
            description = "Don't open the report in browser after generation"
        )
        private boolean noOpen = false;

        @Override
        public Integer call() throws Exception {
            System.out.println("=== SORT MODE" + (dryRun ? " (dry-run)" : "") + " ===");
            System.out.println("Input:  " + inputDir);
            System.out.println("Output: " + outputDir);
            System.out.println();

            // Clean up and create reports directory
            deleteDir(reportsDir.toFile());
            Files.createDirectories(reportsDir);

            // Create configuration
            SortConfiguration config = new SortConfiguration(inputDir, outputDir);
            config.setDebugMode(dryRun);

            // Create engine and execute
            AudioSorterEngine engine = new AudioSorterEngine(config);
            engine.addListener(new ConsoleProgressListener());
            RunTotals totals = engine.execute();

            // Generate reports
            generateReports(totals, config, reportsDir);

            if (!noOpen) {
                openReport(reportsDir.resolve("report.html").toFile());
            }

            return 0;
        }
    }

    // Shared utility methods

    static void generateReports(RunTotals totals, SortConfiguration config, Path reportsDir) {
        // Generate HTML report (includes catalog.html)
        try {
            HtmlReportGenerator htmlGenerator = new HtmlReportGenerator();
            Path htmlReport = reportsDir.resolve("report.html");
            htmlGenerator.generate(totals, config, htmlReport);
            System.out.println("Generated HTML report: " + htmlReport.toAbsolutePath());
            System.out.println("Generated catalog: " + reportsDir.resolve("catalog.html").toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to generate HTML report: " + e.getMessage());
            e.printStackTrace();
        }

        // Generate JSON report
        try {
            JsonReportGenerator jsonGenerator = new JsonReportGenerator();
            Path jsonReport = reportsDir.resolve("report.json");
            jsonGenerator.generate(totals, config, jsonReport);
            System.out.println("Generated JSON report: " + jsonReport.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to generate JSON report: " + e.getMessage());
        }
    }

    static void openReport(File reportFile) {
        if (Desktop.isDesktopSupported() && reportFile.exists()) {
            try {
                Desktop.getDesktop().open(reportFile);
            } catch (IOException e) {
                System.err.println("Could not open report file: " + e.getMessage());
            }
        }
    }

    static void deleteDir(File dir) {
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
