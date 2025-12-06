package io.github.warnotte.audiosorter.core;

import io.github.warnotte.audiosorter.listener.SortProgressListener;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Sorts/copies audio files based on scan results.
 * Uses the data from MusicScanner to organize files into a new directory structure.
 */
public class MusicSorter {

    private final Path outputDirectory;
    private final List<SortProgressListener> listeners = new ArrayList<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private int processedFiles = 0;

    public MusicSorter(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public MusicSorter(SortConfiguration config) {
        this.outputDirectory = config.getOutputDirectory();
    }

    public void addListener(SortProgressListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SortProgressListener listener) {
        listeners.remove(listener);
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sort files based on scan results.
     * @param scanResults The results from MusicScanner.scan()
     * @return Updated RunTotals with copy information
     */
    public RunTotals sort(RunTotals scanResults) {
        if (outputDirectory == null) {
            throw new IllegalStateException("Output directory is required");
        }

        cancelled.set(false);
        processedFiles = 0;

        // Create output directory
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            fireOnError("Failed to create output directory", e);
            return scanResults;
        }

        fireOnSortStarted(null, outputDirectory, false);

        // Process each directory report
        for (DirectoryReport report : scanResults.getDirectoryReports()) {
            if (cancelled.get()) {
                break;
            }

            // Skip empty directories or directories without audio files
            if (report.isEmpty() || report.getScannedFilesCount() == 0) {
                continue;
            }

            // Build destination path
            String artist = filterInvalidCharacters(report.getArtist());
            String album = filterInvalidCharacters(report.getAlbum());
            String year = filterInvalidCharacters(report.getYear());
            String albumFolder = buildAlbumFolderName(album, report);

            Path destArtistDir = outputDirectory.resolve(artist);
            Path destAlbumDir = destArtistDir.resolve("[" + year + "] " + albumFolder);
            report.setDestinationPath(destAlbumDir);

            // Create destination and copy files
            try {
                Files.createDirectories(destAlbumDir);
            } catch (IOException e) {
                fireOnError("Failed to create destination directory: " + destAlbumDir, e);
                continue;
            }

            copyFiles(report.getPath().toFile(), destAlbumDir, report);

            fireOnDirectoryCompleted(report);
        }

        fireOnSortCompleted(scanResults);
        return scanResults;
    }

    private String buildAlbumFolderName(String album, DirectoryReport report) {
        StringBuilder sb = new StringBuilder(album);

        if (report.getFormat() != null) {
            sb.append(" - [").append(report.getFormat());

            if (report.isVariableBitrate()) {
                sb.append(" VBR");
            }

            if (report.getBitrate() != null) {
                if (report.isVariableBitrate()) {
                    sb.append(" ~");
                } else {
                    sb.append(" ");
                }
                sb.append(report.getBitrate()).append(" kBps");
            }

            if (report.getSampleRate() != null) {
                sb.append(" ").append(report.getSampleRate()).append(" kHz");
            }

            sb.append("]");
        }

        return sb.toString();
    }

    private void copyFiles(File sourceDir, Path destDir, DirectoryReport report) {
        File[] files = sourceDir.listFiles(File::isFile);
        if (files == null) return;

        for (File file : files) {
            if (cancelled.get()) {
                break;
            }

            Path source = file.toPath();
            Path dest = destDir.resolve(file.getName());

            FileReport fileReport = new FileReport(source);
            fileReport.setDestination(dest);
            report.addFile(fileReport);

            fireOnFileCopyStarted(source, dest);
            Instant start = Instant.now();

            try {
                Files.copy(source, dest, REPLACE_EXISTING);
                Duration duration = Duration.between(start, Instant.now());
                fileReport.markCopied(dest, duration);
                fireOnFileCopied(fileReport);
            } catch (Exception e) {
                fileReport.markFailed(e.getMessage());
                fireOnFileCopyFailed(fileReport);
            }

            processedFiles++;
        }
    }

    private String filterInvalidCharacters(String str) {
        if (str == null) return "UNKNOWN";

        // Replace invalid filesystem characters
        str = str.replaceAll("[/?%*:|\"<>\\\\]", "_");
        str = str.replace("/", "-");
        str = str.replace("|", "-");
        str = str.replace("?", "-");
        str = str.replace("'", " ");
        str = str.replace("*", "");
        str = str.replace(">", ")");
        str = str.replace("<", "(");
        str = str.replace("\\", "-");

        // Trim leading/trailing spaces and dots (Windows doesn't allow them)
        str = str.trim();
        while (str.endsWith(".")) {
            str = str.substring(0, str.length() - 1).trim();
        }

        return str;
    }

    // Event firing methods

    private void fireOnSortStarted(Path inputDir, Path outputDir, boolean debugMode) {
        for (SortProgressListener l : listeners) {
            l.onSortStarted(inputDir, outputDir, debugMode);
        }
    }

    private void fireOnFileCopyStarted(Path source, Path dest) {
        for (SortProgressListener l : listeners) {
            l.onFileCopyStarted(source, dest);
        }
    }

    private void fireOnFileCopied(FileReport report) {
        for (SortProgressListener l : listeners) {
            l.onFileCopied(report);
        }
    }

    private void fireOnFileCopyFailed(FileReport report) {
        for (SortProgressListener l : listeners) {
            l.onFileCopyFailed(report);
        }
    }

    private void fireOnDirectoryCompleted(DirectoryReport report) {
        for (SortProgressListener l : listeners) {
            l.onDirectoryCompleted(report);
        }
    }

    private void fireOnSortCompleted(RunTotals totals) {
        for (SortProgressListener l : listeners) {
            l.onSortCompleted(totals);
        }
    }

    private void fireOnError(String message, Exception e) {
        for (SortProgressListener l : listeners) {
            l.onError(message, e);
        }
    }
}
