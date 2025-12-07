package io.github.warnotte.audiosorter.gui;

import atlantafx.base.theme.*;
import io.github.warnotte.audiosorter.core.MusicScanner;
import io.github.warnotte.audiosorter.core.MusicSorter;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.coverart.CoverArtExtractor;
import io.github.warnotte.audiosorter.coverart.CoverArtReport;
import io.github.warnotte.audiosorter.coverart.CoverArtReportGenerator;
import io.github.warnotte.audiosorter.coverart.MusicBrainzFetcher;
import io.github.warnotte.audiosorter.listener.ScanProgressListener;
import io.github.warnotte.audiosorter.listener.SortProgressListener;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;
import io.github.warnotte.audiosorter.report.HtmlReportGenerator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Main controller for the AudioFilesSorter GUI.
 */
public class MainController {

    @FXML private RadioButton scanModeRadio;
    @FXML private RadioButton sortModeRadio;
    @FXML private RadioButton coverArtModeRadio;
    @FXML private TextField sourceFolderField;
    @FXML private TextField destFolderField;
    @FXML private VBox destFolderBox;
    @FXML private CheckBox openReportCheckbox;
    @FXML private CheckBox onlineSearchCheckbox;
    @FXML private Button startButton;
    @FXML private Button cancelButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label progressPercent;
    @FXML private Label currentItemLabel;
    @FXML private TitledPane statsPane;
    @FXML private Label statsDirsScanned;
    @FXML private Label statsAudioFiles;
    @FXML private Label statsDirsOk;
    @FXML private Label statsMissingTags;
    @FXML private Label statsErrors;
    @FXML private Label statsDuplicates;
    @FXML private TextArea logArea;
    @FXML private ComboBox<String> themeComboBox;

    private Stage stage;
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private Task<?> currentTask;
    private MusicScanner currentScanner;
    private MusicSorter currentSorter;
    private Path reportsPath;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        // Toggle destination folder visibility based on mode
        scanModeRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                destFolderBox.setVisible(false);
                destFolderBox.setManaged(false);
                onlineSearchCheckbox.setVisible(false);
                onlineSearchCheckbox.setManaged(false);
                openReportCheckbox.setVisible(true);
                openReportCheckbox.setManaged(true);
                startButton.setText("Start Scan");
            }
        });

        sortModeRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                destFolderBox.setVisible(true);
                destFolderBox.setManaged(true);
                onlineSearchCheckbox.setVisible(false);
                onlineSearchCheckbox.setManaged(false);
                openReportCheckbox.setVisible(true);
                openReportCheckbox.setManaged(true);
                startButton.setText("Start Sort");
            }
        });

        coverArtModeRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                destFolderBox.setVisible(false);
                destFolderBox.setManaged(false);
                onlineSearchCheckbox.setVisible(true);
                onlineSearchCheckbox.setManaged(true);
                openReportCheckbox.setVisible(false);
                openReportCheckbox.setManaged(false);
                startButton.setText("Extract Covers");
            }
        });

        // Initialize theme selector
        themes.put("Dracula", new Dracula());
        themes.put("Nord Dark", new NordDark());
        themes.put("Nord Light", new NordLight());
        themes.put("Primer Dark", new PrimerDark());
        themes.put("Primer Light", new PrimerLight());
        themes.put("Cupertino Dark", new CupertinoDark());
        themes.put("Cupertino Light", new CupertinoLight());
        themeComboBox.getItems().addAll(themes.keySet());
        themeComboBox.setValue("Dracula");
    }

    @FXML
    private void changeTheme() {
        String selected = themeComboBox.getValue();
        if (selected != null && themes.containsKey(selected)) {
            Application.setUserAgentStylesheet(themes.get(selected).getUserAgentStylesheet());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void browseSourceFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Source Folder");
        if (!sourceFolderField.getText().isEmpty()) {
            File current = new File(sourceFolderField.getText());
            if (current.exists()) {
                chooser.setInitialDirectory(current);
            }
        }
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            sourceFolderField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void browseDestFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Destination Folder");
        if (!destFolderField.getText().isEmpty()) {
            File current = new File(destFolderField.getText());
            if (current.exists()) {
                chooser.setInitialDirectory(current);
            }
        }
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            destFolderField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void startOperation() {
        String source = sourceFolderField.getText().trim();
        if (source.isEmpty()) {
            showError("Please select a source folder");
            return;
        }

        Path sourcePath = Path.of(source);
        if (!sourcePath.toFile().exists()) {
            showError("Source folder does not exist: " + source);
            return;
        }

        boolean isScanOnly = scanModeRadio.isSelected();
        boolean isCoverArtMode = coverArtModeRadio.isSelected();

        if (!isScanOnly && !isCoverArtMode) {
            String dest = destFolderField.getText().trim();
            if (dest.isEmpty()) {
                showError("Please select a destination folder for sort mode");
                return;
            }
        }

        // Reset UI
        resetStats();
        logArea.clear();
        progressBar.setProgress(0);
        progressPercent.setText("");
        currentItemLabel.setText("");
        statsPane.setExpanded(true);

        // Disable controls
        setControlsDisabled(true);

        if (isCoverArtMode) {
            startCoverArtExtraction(sourcePath);
        } else if (isScanOnly) {
            startScan(sourcePath);
        } else {
            startSort(sourcePath, Path.of(destFolderField.getText().trim()));
        }
    }

    private void startScan(Path sourcePath) {
        reportsPath = Path.of("reports");

        Task<RunTotals> task = new Task<>() {
            @Override
            protected RunTotals call() throws Exception {
                currentScanner = new MusicScanner(sourcePath);
                currentScanner.addListener(new GuiScanListener());
                return currentScanner.scan();
            }

            @Override
            protected void succeeded() {
                RunTotals totals = getValue();
                generateReports(totals, sourcePath, null);
                onOperationComplete("Scan completed successfully!");
            }

            @Override
            protected void failed() {
                log("ERROR: " + getException().getMessage());
                onOperationComplete("Scan failed!", false);
            }

            @Override
            protected void cancelled() {
                onOperationComplete("Scan cancelled", false);
            }
        };

        currentTask = task;
        new Thread(task).start();
    }

    private void startSort(Path sourcePath, Path destPath) {
        reportsPath = Path.of("reports");

        Task<RunTotals> task = new Task<>() {
            @Override
            protected RunTotals call() throws Exception {
                // First scan
                currentScanner = new MusicScanner(sourcePath);
                currentScanner.addListener(new GuiScanListener());
                RunTotals totals = currentScanner.scan();

                if (isCancelled()) return totals;

                // Then sort
                int totalFiles = (int) totals.getFilesSeen();
                Platform.runLater(() -> {
                    progressLabel.setText("Copying files...");
                    progressBar.setProgress(0);
                    progressPercent.setText(String.format("0/%d (0%%)", totalFiles));
                });

                currentSorter = new MusicSorter(destPath);
                GuiSortListener sortListener = new GuiSortListener();
                sortListener.setTotalFiles(totalFiles);
                currentSorter.addListener(sortListener);
                currentSorter.sort(totals);

                return totals;
            }

            @Override
            protected void succeeded() {
                RunTotals totals = getValue();
                generateReports(totals, sourcePath, destPath);
                onOperationComplete("Sort completed successfully!");
            }

            @Override
            protected void failed() {
                log("ERROR: " + getException().getMessage());
                getException().printStackTrace();
                onOperationComplete("Sort failed!", false);
            }

            @Override
            protected void cancelled() {
                onOperationComplete("Sort cancelled", false);
            }
        };

        currentTask = task;
        new Thread(task).start();
    }

    private void startCoverArtExtraction(Path sourcePath) {
        boolean useOnline = onlineSearchCheckbox.isSelected();
        reportsPath = Path.of("reports");

        Task<CoverArtReport> task = new Task<>() {
            @Override
            protected CoverArtReport call() throws Exception {
                CoverArtExtractor extractor = new CoverArtExtractor();
                MusicBrainzFetcher fetcher = useOnline ? new MusicBrainzFetcher() : null;
                CoverArtReport report = new CoverArtReport(sourcePath);

                Platform.runLater(() -> {
                    progressLabel.setText("Finding directories...");
                    log("Scanning for directories with audio files...");
                });

                List<Path> directories;
                try (Stream<Path> paths = Files.walk(sourcePath)) {
                    directories = paths
                            .filter(Files::isDirectory)
                            .filter(MainController.this::hasAudioFiles)
                            .toList();
                }

                int totalDirs = directories.size();
                report.setTotalDirectories(totalDirs);
                Platform.runLater(() -> {
                    log("Found " + totalDirs + " directories with audio files");
                    progressLabel.setText("Phase 1: Extracting from tags...");
                    statsDirsScanned.setText(String.valueOf(totalDirs));
                });

                List<Path> dirsWithoutCover = new ArrayList<>();
                int processed = 0;

                // Phase 1: Extract from embedded tags
                for (Path dir : directories) {
                    if (isCancelled()) break;

                    processed++;
                    String relativePath = sourcePath.relativize(dir).toString();
                    if (relativePath.isEmpty()) relativePath = ".";

                    final int p = processed;
                    final String rp = relativePath;
                    Platform.runLater(() -> {
                        currentItemLabel.setText(rp);
                        double progress = (double) p / totalDirs * (useOnline ? 0.5 : 1.0);
                        progressBar.setProgress(progress);
                        progressPercent.setText(String.format("%d/%d", p, totalDirs));
                    });

                    CoverArtExtractor.ExtractionResult result = extractor.extractCover(dir);
                    CoverArtExtractor.AlbumInfo info = extractor.getAlbumInfo(dir);

                    switch (result) {
                        case EXTRACTED:
                            log("[OK] " + relativePath + " -> cover extracted from tags");
                            // Find the cover file that was just extracted
                            Path coverFile = findCoverFile(dir);
                            if (coverFile != null) {
                                long size = Files.size(coverFile);
                                report.addExtractedFromTags(new CoverArtReport.CoverResult(
                                    coverFile, dir, info.artist, info.album, size, "tags"));
                            }
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

                report.setAlreadyHadCover(extractor.getSkippedAlreadyExists());

                // Phase 2: Online fetch if enabled
                if (useOnline && !dirsWithoutCover.isEmpty() && !isCancelled()) {
                    Platform.runLater(() -> {
                        progressLabel.setText("Phase 2: Fetching from MusicBrainz...");
                        log("Directories without embedded cover: " + dirsWithoutCover.size());
                        log("(Rate limited to 1 request/sec)");
                    });

                    int onlineProcessed = 0;
                    for (Path dir : dirsWithoutCover) {
                        if (isCancelled()) break;

                        onlineProcessed++;
                        String relativePath = sourcePath.relativize(dir).toString();
                        if (relativePath.isEmpty()) relativePath = ".";

                        final int op = onlineProcessed;
                        final int dwc = dirsWithoutCover.size();
                        final String rp = relativePath;
                        Platform.runLater(() -> {
                            currentItemLabel.setText(rp);
                            double progress = 0.5 + (double) op / dwc * 0.5;
                            progressBar.setProgress(progress);
                            progressPercent.setText(String.format("Online %d/%d", op, dwc));
                        });

                        CoverArtExtractor.AlbumInfo info = extractor.getAlbumInfo(dir);
                        if (!info.isValid()) {
                            log("[SKIP] " + relativePath + " -> no artist/album tags");
                            report.addMissingCover(new CoverArtReport.MissingCover(dir, null, null, "no_tags"));
                            continue;
                        }

                        log("[....] " + relativePath + " -> searching \"" + info.artist + " - " + info.album + "\"...");

                        Optional<Path> cover = fetcher.fetchCover(info.artist, info.album, dir);
                        if (cover.isPresent()) {
                            long size = Files.size(cover.get());
                            log("[OK] " + relativePath + " -> downloaded from MusicBrainz (" + formatSize(size) + ")");
                            report.addDownloadedOnline(new CoverArtReport.CoverResult(
                                cover.get(), dir, info.artist, info.album, size, "musicbrainz"));
                        } else {
                            log("[MISS] " + relativePath + " -> not found on MusicBrainz");
                            report.addMissingCover(new CoverArtReport.MissingCover(dir, info.artist, info.album, "not_found"));
                        }
                    }
                } else if (!dirsWithoutCover.isEmpty()) {
                    for (Path dir : dirsWithoutCover) {
                        String relativePath = sourcePath.relativize(dir).toString();
                        if (relativePath.isEmpty()) relativePath = ".";
                        log("[SKIP] " + relativePath + " -> no embedded cover");
                        CoverArtExtractor.AlbumInfo info = extractor.getAlbumInfo(dir);
                        report.addMissingCover(new CoverArtReport.MissingCover(
                            dir, info.artist, info.album, "no_embedded"));
                    }
                }

                return report;
            }

            @Override
            protected void succeeded() {
                CoverArtReport report = getValue();
                int totalExtracted = report.getTotalExtracted();

                log("");
                log("=== Results ===");
                log("Directories scanned:    " + report.getTotalDirectories());
                log("Already had cover:      " + report.getAlreadyHadCover());
                log("Extracted from tags:    " + report.getExtractedFromTags().size());
                if (useOnline) {
                    log("Downloaded online:      " + report.getDownloadedOnline().size());
                }
                log("Still missing:          " + report.getMissingCovers().size());

                // Generate HTML report
                try {
                    Files.createDirectories(reportsPath);
                    Path reportPath = reportsPath.resolve("coverart-report.html");
                    new CoverArtReportGenerator().generate(report, reportPath);
                    log("Report generated: " + reportPath.toAbsolutePath());

                    // Open report in browser
                    if (totalExtracted > 0 || !report.getMissingCovers().isEmpty()) {
                        Desktop.getDesktop().open(reportPath.toFile());
                    }
                } catch (IOException e) {
                    log("ERROR generating report: " + e.getMessage());
                }

                if (totalExtracted > 0) {
                    onOperationComplete("SUCCESS: Got " + totalExtracted + " cover(s)! Report opened.");
                } else if (!report.getMissingCovers().isEmpty() && !useOnline) {
                    onOperationComplete("Done. Use 'Search MusicBrainz' option for missing covers.");
                } else {
                    onOperationComplete("Done. All directories already have cover images.");
                }

                // Update stats display
                Platform.runLater(() -> {
                    statsDirsOk.setText(String.valueOf(report.getAlreadyHadCover()));
                    statsAudioFiles.setText(String.valueOf(totalExtracted));
                    statsMissingTags.setText(String.valueOf(report.getMissingCovers().size()));
                });
            }

            @Override
            protected void failed() {
                log("ERROR: " + getException().getMessage());
                getException().printStackTrace();
                onOperationComplete("Cover art extraction failed!", false);
            }

            @Override
            protected void cancelled() {
                onOperationComplete("Cover art extraction cancelled", false);
            }
        };

        currentTask = task;
        new Thread(task).start();
    }

    private Path findCoverFile(Path dir) {
        String[] coverNames = {"cover.jpg", "cover.png", "cover.gif", "cover.bmp"};
        for (String name : coverNames) {
            Path coverPath = dir.resolve(name);
            if (Files.exists(coverPath)) {
                return coverPath;
            }
        }
        return null;
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

    private void generateReports(RunTotals totals, Path sourcePath, Path destPath) {
        try {
            log("Generating reports...");

            // Ensure reports directory exists
            java.nio.file.Files.createDirectories(reportsPath);

            SortConfiguration config = new SortConfiguration(sourcePath, destPath != null ? destPath : sourcePath);

            HtmlReportGenerator generator = new HtmlReportGenerator();
            Path reportPath = reportsPath.resolve("report.html");
            generator.generate(totals, config, reportPath);
            log("Report generated: " + reportPath.toAbsolutePath());

            if (openReportCheckbox.isSelected()) {
                Desktop.getDesktop().open(reportPath.toFile());
            }
        } catch (Exception e) {
            log("ERROR generating report: " + e.getMessage());
        }
    }

    @FXML
    private void cancelOperation() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
            // Also cancel the scanner/sorter directly
            if (currentScanner != null) {
                currentScanner.cancel();
            }
            if (currentSorter != null) {
                currentSorter.cancel();
            }
            log("Cancellation requested...");
        }
    }

    @FXML
    private void openReportsFolder() {
        try {
            Path reports = Path.of("reports");
            if (reports.toFile().exists()) {
                Desktop.getDesktop().open(reports.toFile());
            } else {
                showError("Reports folder does not exist yet. Run a scan first.");
            }
        } catch (Exception e) {
            showError("Could not open reports folder: " + e.getMessage());
        }
    }

    private void onOperationComplete(String message) {
        onOperationComplete(message, true);
    }

    private void onOperationComplete(String message, boolean success) {
        Platform.runLater(() -> {
            progressLabel.setText(message);
            if (success) {
                progressBar.setProgress(1.0);
                progressPercent.setText("100%");
            } else {
                // Keep current progress for cancelled/failed operations
                progressPercent.setText("Cancelled");
            }
            setControlsDisabled(false);
            log(message);
        });
    }

    private void setControlsDisabled(boolean disabled) {
        sourceFolderField.setDisable(disabled);
        destFolderField.setDisable(disabled);
        scanModeRadio.setDisable(disabled);
        sortModeRadio.setDisable(disabled);
        coverArtModeRadio.setDisable(disabled);
        openReportCheckbox.setDisable(disabled);
        onlineSearchCheckbox.setDisable(disabled);
        startButton.setDisable(disabled);
        cancelButton.setDisable(!disabled);
    }

    private void resetStats() {
        statsDirsScanned.setText("0");
        statsAudioFiles.setText("0");
        statsDirsOk.setText("0");
        statsMissingTags.setText("0");
        statsErrors.setText("0");
        statsDuplicates.setText("0");
    }

    private void log(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Listener for scan progress updates - updates the JavaFX UI.
     */
    private class GuiScanListener implements ScanProgressListener {
        private int dirsScanned = 0;
        private int audioFiles = 0;
        private int dirsOk = 0;
        private int dirsMissingTags = 0;
        private int errors = 0;

        @Override
        public void onScanStarted(Path inputDirectory) {
            Platform.runLater(() -> {
                progressLabel.setText("Scanning...");
                log("Scan started: " + inputDirectory);
            });
        }

        @Override
        public void onDirectoryEntered(Path directory, int depth) {
            Platform.runLater(() -> {
                currentItemLabel.setText(directory.toString());
            });
        }

        @Override
        public void onFilesFound(Path directory, int count) {
            audioFiles += count;
            Platform.runLater(() -> {
                statsAudioFiles.setText(String.valueOf(audioFiles));
            });
        }

        @Override
        public void onDirectoryCompleted(DirectoryReport report) {
            dirsScanned++;
            if (report.isTagFound()) {
                dirsOk++;
            } else if (!report.isEmpty()) {
                dirsMissingTags++;
            }

            Platform.runLater(() -> {
                statsDirsScanned.setText(String.valueOf(dirsScanned));
                statsDirsOk.setText(String.valueOf(dirsOk));
                statsMissingTags.setText(String.valueOf(dirsMissingTags));
            });
        }

        @Override
        public void onProgressUpdate(int processedDirs, int totalDirs) {
            Platform.runLater(() -> {
                if (totalDirs > 0) {
                    double progress = (double) processedDirs / totalDirs;
                    progressBar.setProgress(progress);
                    progressPercent.setText(String.format("%.0f%%", progress * 100));
                } else {
                    // Unknown total - show indeterminate progress
                    progressBar.setProgress(-1); // Indeterminate
                    progressPercent.setText(processedDirs + " dirs");
                }
            });
        }

        @Override
        public void onScanCompleted(RunTotals totals) {
            Platform.runLater(() -> {
                progressLabel.setText("Scan complete");
                progressBar.setProgress(1.0);
                progressPercent.setText("100%");
                currentItemLabel.setText("");

                // Update final stats from totals
                long audioFilesTotal = totals.getFilesSeen();
                statsAudioFiles.setText(String.valueOf(audioFilesTotal));

                log("Scan complete: " + totals.getDirectoryReports().size() + " directories, " +
                    audioFilesTotal + " audio files");
            });
        }

        @Override
        public void onError(String message, Exception e) {
            errors++;
            Platform.runLater(() -> {
                statsErrors.setText(String.valueOf(errors));
                log("ERROR: " + message);
            });
        }
    }

    /**
     * Listener for sort progress updates.
     */
    private class GuiSortListener implements SortProgressListener {
        private int copiedFiles = 0;
        private int totalFiles = 0;

        public void setTotalFiles(int total) {
            this.totalFiles = total;
        }

        @Override
        public void onSortStarted(Path inputDir, Path outputDir, boolean debugMode) {
            Platform.runLater(() -> {
                progressLabel.setText("Copying files...");
                log("Starting to copy files to " + outputDir + "...");
            });
        }

        @Override
        public void onFileCopyStarted(Path source, Path dest) {
            Platform.runLater(() -> {
                currentItemLabel.setText(source.getFileName().toString());
            });
        }

        @Override
        public void onFileCopied(FileReport report) {
            copiedFiles++;
            Platform.runLater(() -> {
                if (totalFiles > 0) {
                    double progress = (double) copiedFiles / totalFiles;
                    progressBar.setProgress(progress);
                    progressPercent.setText(String.format("%d/%d (%.0f%%)", copiedFiles, totalFiles, progress * 100));
                } else {
                    progressPercent.setText(copiedFiles + " files");
                }
            });
        }

        @Override
        public void onFileCopyFailed(FileReport report) {
            Platform.runLater(() -> {
                log("FAILED: " + report.getSource().getFileName() + " - " + report.getErrorMessage());
            });
        }

        @Override
        public void onSortCompleted(RunTotals totals) {
            Platform.runLater(() -> {
                progressLabel.setText("Copy complete");
                progressBar.setProgress(1.0);
                progressPercent.setText("100%");
                currentItemLabel.setText("");
                log(String.format("Copy complete: %d files copied, %d failed, %.2f MB",
                    totals.getFilesCopied(), totals.getFilesFailed(),
                    totals.getTotalBytesCopied() / (1024.0 * 1024.0)));
            });
        }

        @Override
        public void onError(String message, Exception e) {
            Platform.runLater(() -> {
                log("ERROR: " + message);
            });
        }
    }
}
