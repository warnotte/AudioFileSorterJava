package io.github.warnotte.audiosorter.gui;

import io.github.warnotte.audiosorter.core.MusicScanner;
import io.github.warnotte.audiosorter.core.MusicSorter;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.listener.ScanProgressListener;
import io.github.warnotte.audiosorter.listener.SortProgressListener;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;
import io.github.warnotte.audiosorter.report.HtmlReportGenerator;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main controller for the AudioFilesSorter GUI.
 */
public class MainController {

    @FXML private RadioButton scanModeRadio;
    @FXML private RadioButton sortModeRadio;
    @FXML private TextField sourceFolderField;
    @FXML private TextField destFolderField;
    @FXML private VBox destFolderBox;
    @FXML private CheckBox openReportCheckbox;
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

    private Stage stage;
    private Task<?> currentTask;
    private MusicScanner currentScanner;
    private MusicSorter currentSorter;
    private Path reportsPath;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        // Toggle destination folder visibility based on mode
        scanModeRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            destFolderBox.setVisible(!newVal);
            destFolderBox.setManaged(!newVal);
            startButton.setText(newVal ? "Start Scan" : "Start Sort");
        });
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

        if (!isScanOnly) {
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

        if (isScanOnly) {
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
        openReportCheckbox.setDisable(disabled);
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
