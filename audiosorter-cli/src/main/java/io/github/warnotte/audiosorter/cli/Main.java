package io.github.warnotte.audiosorter.cli;

import io.github.warnotte.audiosorter.core.AudioSorterEngine;
import io.github.warnotte.audiosorter.core.MusicScanner;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.coverart.CoverArtExtractor;
import io.github.warnotte.audiosorter.coverart.MusicBrainzFetcher;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

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
        Main.SortCommand.class,
        Main.CoverArtCommand.class
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

    /**
     * CoverArt command - extract missing album cover art.
     */
    @Command(
        name = "coverart",
        mixinStandardHelpOptions = true,
        description = "Extract missing album cover art from embedded tags or MusicBrainz"
    )
    static class CoverArtCommand implements Callable<Integer> {

        @Parameters(
            index = "0",
            description = "Music directory to scan for missing covers"
        )
        private Path inputDir;

        @Option(
            names = {"-o", "--online"},
            description = "Search MusicBrainz for covers not found in tags (rate limited 1 req/sec)"
        )
        private boolean online = false;

        @Override
        public Integer call() throws Exception {
            System.out.println("=== COVER ART EXTRACTION ===");
            System.out.println("Input: " + inputDir);
            if (online) {
                System.out.println("Online mode: ENABLED (will query MusicBrainz for missing covers)");
            }
            System.out.println();

            CoverArtExtractor extractor = new CoverArtExtractor();
            MusicBrainzFetcher fetcher = online ? new MusicBrainzFetcher() : null;

            int totalDirs = 0;
            int fetchedOnline = 0;
            List<CoverArtExtractor.ExtractedCover> onlineCovers = new ArrayList<>();
            List<String> missingCovers = new ArrayList<>();

            try (Stream<Path> paths = Files.walk(inputDir)) {
                var directories = paths
                        .filter(Files::isDirectory)
                        .filter(this::hasAudioFiles)
                        .toList();

                totalDirs = directories.size();
                System.out.println("Found " + totalDirs + " directories with audio files");
                System.out.println();
                System.out.println("=== Phase 1: Extracting from embedded tags ===");
                System.out.println();

                List<Path> dirsWithoutCover = new ArrayList<>();

                for (Path dir : directories) {
                    CoverArtExtractor.ExtractionResult result = extractor.extractCover(dir);
                    String relativePath = inputDir.relativize(dir).toString();
                    if (relativePath.isEmpty()) relativePath = ".";

                    switch (result) {
                        case EXTRACTED:
                            System.out.println("[OK]   " + relativePath + " -> cover extracted from tags");
                            break;
                        case ALREADY_EXISTS:
                            // Silent
                            break;
                        case NO_EMBEDDED_ART:
                            dirsWithoutCover.add(dir);
                            break;
                        case NO_AUDIO_FILES:
                        case ERROR:
                            break;
                    }
                }

                // Phase 2: Online fetch if enabled
                if (online && !dirsWithoutCover.isEmpty()) {
                    System.out.println();
                    System.out.println("=== Phase 2: Fetching from MusicBrainz ===");
                    System.out.println("Directories without embedded cover: " + dirsWithoutCover.size());
                    System.out.println("(Rate limited to 1 request/sec)");
                    System.out.println();

                    for (Path dir : dirsWithoutCover) {
                        String relativePath = inputDir.relativize(dir).toString();
                        if (relativePath.isEmpty()) relativePath = ".";

                        CoverArtExtractor.AlbumInfo info = extractor.getAlbumInfo(dir);
                        if (!info.isValid()) {
                            System.out.println("[SKIP] " + relativePath + " -> no artist/album tags");
                            missingCovers.add(relativePath + " (no tags)");
                            continue;
                        }

                        System.out.print("[....] " + relativePath + " -> searching \"" + info.artist + " - " + info.album + "\"...");
                        System.out.flush();

                        Optional<Path> cover = fetcher.fetchCover(info.artist, info.album, dir);
                        if (cover.isPresent()) {
                            long size = Files.size(cover.get());
                            System.out.println("\r[OK]   " + relativePath + " -> downloaded from MusicBrainz (" + formatSize(size) + ")");
                            fetchedOnline++;
                            onlineCovers.add(new CoverArtExtractor.ExtractedCover(cover.get(), size, ".jpg"));
                        } else {
                            System.out.println("\r[MISS] " + relativePath + " -> not found on MusicBrainz");
                            missingCovers.add(relativePath + " (" + info.artist + " - " + info.album + ")");
                        }
                    }
                } else if (!dirsWithoutCover.isEmpty()) {
                    for (Path dir : dirsWithoutCover) {
                        String relativePath = inputDir.relativize(dir).toString();
                        if (relativePath.isEmpty()) relativePath = ".";
                        System.out.println("[SKIP] " + relativePath + " -> no embedded cover");
                        missingCovers.add(relativePath);
                    }
                }
            }

            // Print summary
            System.out.println();
            System.out.println("=== Results ===");
            System.out.println("Directories scanned:    " + totalDirs);
            System.out.println("Already had cover:      " + extractor.getSkippedAlreadyExists());
            System.out.println("Extracted from tags:    " + extractor.getExtracted());
            if (online) {
                System.out.println("Downloaded online:      " + fetchedOnline);
            }
            System.out.println("Still missing:          " + missingCovers.size());

            // List extracted covers
            if (!extractor.getExtractedCovers().isEmpty()) {
                System.out.println();
                System.out.println("=== Covers Extracted from Tags ===");
                for (CoverArtExtractor.ExtractedCover cover : extractor.getExtractedCovers()) {
                    System.out.println("  " + cover.coverPath + " (" + cover.getSizeFormatted() + ")");
                }
            }

            if (!onlineCovers.isEmpty()) {
                System.out.println();
                System.out.println("=== Covers Downloaded from MusicBrainz ===");
                for (CoverArtExtractor.ExtractedCover cover : onlineCovers) {
                    System.out.println("  " + cover.coverPath + " (" + cover.getSizeFormatted() + ")");
                }
            }

            System.out.println();
            int totalExtracted = extractor.getExtracted() + fetchedOnline;
            if (totalExtracted > 0) {
                System.out.println("SUCCESS: Got " + totalExtracted + " cover(s)!");
            } else if (!missingCovers.isEmpty() && !online) {
                System.out.println("TIP: Use --online flag to search MusicBrainz for missing covers");
            } else if (!missingCovers.isEmpty()) {
                System.out.println("Some covers could not be found. Try manual search or different tags.");
            } else {
                System.out.println("All directories already have cover images.");
            }

            return 0;
        }

        private boolean hasAudioFiles(Path dir) {
            try (Stream<Path> files = Files.list(dir)) {
                return files.anyMatch(f -> {
                    String name = f.getFileName().toString().toLowerCase();
                    return name.endsWith(".mp3") || name.endsWith(".flac") ||
                           name.endsWith(".ogg") || name.endsWith(".m4a") ||
                           name.endsWith(".wma") || name.endsWith(".wav");
                });
            } catch (IOException e) {
                return false;
            }
        }

        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
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
