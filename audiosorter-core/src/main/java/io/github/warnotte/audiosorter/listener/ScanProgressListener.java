package io.github.warnotte.audiosorter.listener;

import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.nio.file.Path;

/**
 * Listener interface for scan progress events.
 * Implement this to receive updates during directory scanning.
 * This is for scan-only operations (no file copying).
 */
public interface ScanProgressListener {

    /**
     * Called when scanning starts.
     */
    default void onScanStarted(Path inputDirectory) {}

    /**
     * Called when entering a directory.
     */
    default void onDirectoryEntered(Path directory, int depth) {}

    /**
     * Called when audio files are found in a directory.
     */
    default void onFilesFound(Path directory, int count) {}

    /**
     * Called when a directory is empty (no audio files and no subdirectories).
     */
    default void onDirectoryEmpty(Path directory) {}

    /**
     * Called when tags are successfully read from a directory.
     */
    default void onTagsRead(Path directory, String artist, String album, String year) {}

    /**
     * Called when a directory has no valid tags.
     */
    default void onTagsMissing(Path directory) {}

    /**
     * Called when a directory has been fully processed.
     */
    default void onDirectoryCompleted(DirectoryReport report) {}

    /**
     * Called periodically with progress updates.
     */
    default void onProgressUpdate(int processedDirs, int totalDirs) {}

    /**
     * Called when the scan is complete.
     */
    default void onScanCompleted(RunTotals totals) {}

    /**
     * Called when an error occurs.
     */
    default void onError(String message, Exception e) {}
}
