package io.github.warnotte.audiosorter.listener;

import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.nio.file.Path;

/**
 * Listener interface for audio sorting progress events.
 * Implement this interface to receive notifications during the sorting process.
 *
 * For CLI: log messages to console
 * For GUI: update progress bars, list views, etc.
 */
public interface SortProgressListener {

    /**
     * Called when the sorting process starts.
     */
    default void onSortStarted(Path inputDir, Path outputDir, boolean debugMode) {}

    /**
     * Called when entering a new directory for scanning.
     */
    default void onDirectoryEntered(Path directory, int depth) {}

    /**
     * Called when audio files are found in a directory.
     */
    default void onFilesFound(Path directory, int count) {}

    /**
     * Called when a directory is empty (no audio files).
     */
    default void onDirectoryEmpty(Path directory) {}

    /**
     * Called when metadata tags are successfully read from a directory.
     */
    default void onTagsRead(Path directory, String artist, String album, String year) {}

    /**
     * Called when no metadata tags are found in a directory.
     */
    default void onTagsMissing(Path directory) {}

    /**
     * Called when a string is filtered for invalid filesystem characters.
     */
    default void onStringFiltered(String original, String filtered, Path directory) {}

    /**
     * Called before a file copy operation starts.
     */
    default void onFileCopyStarted(Path source, Path destination) {}

    /**
     * Called when a file is successfully copied.
     */
    default void onFileCopied(FileReport fileReport) {}

    /**
     * Called when a file copy operation fails.
     */
    default void onFileCopyFailed(FileReport fileReport) {}

    /**
     * Called when a directory processing is completed.
     */
    default void onDirectoryCompleted(DirectoryReport report) {}

    /**
     * Called to report overall progress.
     * @param processedDirs number of directories processed so far
     * @param totalDirs total number of directories (may be -1 if unknown)
     * @param processedFiles number of files processed so far
     */
    default void onProgressUpdate(int processedDirs, int totalDirs, int processedFiles) {}

    /**
     * Called when the sorting process completes.
     */
    default void onSortCompleted(RunTotals totals) {}

    /**
     * Called when an unexpected error occurs.
     */
    default void onError(String message, Exception exception) {}

    /**
     * Called for general informational messages.
     */
    default void onInfo(String message) {}

    /**
     * Called for warning messages.
     */
    default void onWarning(String message) {}
}
