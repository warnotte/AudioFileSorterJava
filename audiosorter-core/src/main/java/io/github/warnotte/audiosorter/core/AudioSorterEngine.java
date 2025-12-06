package io.github.warnotte.audiosorter.core;

import io.github.warnotte.audiosorter.listener.ScanProgressListener;
import io.github.warnotte.audiosorter.listener.SortProgressListener;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main engine for sorting audio files based on metadata.
 * This is a facade that combines MusicScanner and MusicSorter.
 *
 * For scan-only operations (catalog generation), use MusicScanner directly.
 * For sort-only operations (using existing scan data), use MusicSorter directly.
 */
public class AudioSorterEngine {

    private final SortConfiguration config;
    private final MusicScanner scanner;
    private final MusicSorter sorter;
    private final List<SortProgressListener> listeners = new ArrayList<>();

    public AudioSorterEngine(SortConfiguration config) {
        this.config = config;
        this.scanner = new MusicScanner(config);
        this.sorter = new MusicSorter(config);
    }

    public void addListener(SortProgressListener listener) {
        listeners.add(listener);

        // Bridge scan events to sort listener
        scanner.addListener(new ScanToSortListenerBridge(listener, config));
        sorter.addListener(listener);
    }

    public void removeListener(SortProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Cancel the current operation.
     */
    public void cancel() {
        scanner.cancel();
        sorter.cancel();
    }

    /**
     * Check if the operation was cancelled.
     */
    public boolean isCancelled() {
        return scanner.isCancelled() || sorter.isCancelled();
    }

    /**
     * Execute the full sorting process (scan + copy).
     * @return RunTotals containing the results
     */
    public RunTotals execute() {
        config.validate();

        // Fire start event
        for (SortProgressListener l : listeners) {
            l.onSortStarted(config.getInputDirectory(), config.getOutputDirectory(), config.isDebugMode());
        }

        // Phase 1: Scan
        RunTotals results = scanner.scan();

        // Phase 2: Copy (if not debug mode)
        if (!config.isDebugMode()) {
            sorter.sort(results);
        }

        // Fire completion event
        for (SortProgressListener l : listeners) {
            l.onSortCompleted(results);
        }

        return results;
    }

    /**
     * Scan only - no file copying.
     * Use this for catalog generation or analysis.
     * @return RunTotals containing scan results
     */
    public RunTotals scanOnly() {
        return scanner.scan();
    }

    /**
     * Get the underlying scanner for direct access.
     */
    public MusicScanner getScanner() {
        return scanner;
    }

    /**
     * Get the underlying sorter for direct access.
     */
    public MusicSorter getSorter() {
        return sorter;
    }

    /**
     * Bridge class to adapt ScanProgressListener events to SortProgressListener.
     */
    private static class ScanToSortListenerBridge implements ScanProgressListener {
        private final SortProgressListener delegate;
        private final SortConfiguration config;

        ScanToSortListenerBridge(SortProgressListener delegate, SortConfiguration config) {
            this.delegate = delegate;
            this.config = config;
        }

        @Override
        public void onScanStarted(Path inputDirectory) {
            // Start event is fired by AudioSorterEngine directly
        }

        @Override
        public void onDirectoryEntered(Path directory, int depth) {
            delegate.onDirectoryEntered(directory, depth);
        }

        @Override
        public void onFilesFound(Path directory, int count) {
            delegate.onFilesFound(directory, count);
        }

        @Override
        public void onDirectoryEmpty(Path directory) {
            delegate.onDirectoryEmpty(directory);
        }

        @Override
        public void onTagsRead(Path directory, String artist, String album, String year) {
            delegate.onTagsRead(directory, artist, album, year);
        }

        @Override
        public void onTagsMissing(Path directory) {
            delegate.onTagsMissing(directory);
        }

        @Override
        public void onDirectoryCompleted(DirectoryReport report) {
            // Don't forward - sorter will send this after copying
        }

        @Override
        public void onProgressUpdate(int processedDirs, int totalDirs) {
            delegate.onProgressUpdate(processedDirs, totalDirs, 0);
        }

        @Override
        public void onScanCompleted(RunTotals totals) {
            // Completion event is fired by AudioSorterEngine directly
        }

        @Override
        public void onError(String message, Exception e) {
            delegate.onError(message, e);
        }
    }
}
