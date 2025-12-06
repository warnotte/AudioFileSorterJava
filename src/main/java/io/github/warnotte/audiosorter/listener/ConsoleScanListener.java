package io.github.warnotte.audiosorter.listener;

import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.RunTotals;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * Console listener for scan-only operations.
 * Logs progress to console via Log4j.
 */
public class ConsoleScanListener implements ScanProgressListener {

    private static final Logger logger = LogManager.getLogger(ConsoleScanListener.class);

    @Override
    public void onScanStarted(Path inputDirectory) {
        logger.info("========================================");
        logger.info("Starting scan: {}", inputDirectory);
        logger.info("========================================");
    }

    @Override
    public void onDirectoryEntered(Path directory, int depth) {
        String indent = "  ".repeat(depth);
        logger.debug("{}Scanning: {}", indent, directory.getFileName());
    }

    @Override
    public void onFilesFound(Path directory, int count) {
        logger.debug("  Found {} audio files in {}", count, directory.getFileName());
    }

    @Override
    public void onDirectoryEmpty(Path directory) {
        logger.debug("  Empty directory: {}", directory);
    }

    @Override
    public void onTagsRead(Path directory, String artist, String album, String year) {
        logger.info("  [{}] {} - {} ({})",
            directory.getFileName(), artist, album, year);
    }

    @Override
    public void onTagsMissing(Path directory) {
        logger.warn("  No tags found in: {}", directory);
    }

    @Override
    public void onDirectoryCompleted(DirectoryReport report) {
        // Quiet by default
    }

    @Override
    public void onProgressUpdate(int processedDirs, int totalDirs) {
        if (processedDirs % 100 == 0) {
            logger.info("Progress: {} directories scanned", processedDirs);
        }
    }

    @Override
    public void onScanCompleted(RunTotals totals) {
        logger.info("========================================");
        logger.info("Scan completed in {}", totals.getTotalDuration());
        logger.info("Directories scanned: {}", totals.getDirectoriesTotal());
        logger.info("  - With tags: {}", totals.getOkDirs());
        logger.info("  - Without tags: {}", totals.getNoTagDirs());
        logger.info("  - Empty: {}", totals.getEmptyDirs());
        logger.info("Audio files found: {}", totals.getFilesSeen());
        logger.info("========================================");
    }

    @Override
    public void onError(String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage());
    }
}
