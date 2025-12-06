package io.github.warnotte.audiosorter.listener;

import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Comparator;

/**
 * Console/Log4j implementation of SortProgressListener.
 * Outputs progress to the console using Log4j.
 */
public class ConsoleProgressListener implements SortProgressListener {

    private static final Logger logger = LogManager.getLogger(ConsoleProgressListener.class);

    @Override
    public void onSortStarted(Path inputDir, Path outputDir, boolean debugMode) {
        logger.info("===================== SORT STARTED =====================");
        logger.info("Input: {}", inputDir);
        logger.info("Output: {}", outputDir);
        logger.info("Debug mode: {}", debugMode);
        logger.info("=========================================================");
    }

    @Override
    public void onDirectoryEntered(Path directory, int depth) {
        // Log at trace level to avoid too much noise
        logger.trace("Entering directory [depth={}]: {}", depth, directory);
    }

    @Override
    public void onFilesFound(Path directory, int count) {
        logger.info(":) - SCANNING: {} ({} files)", directory, count);
    }

    @Override
    public void onDirectoryEmpty(Path directory) {
        logger.info(":) - Empty directory: {}", directory);
    }

    @Override
    public void onTagsRead(Path directory, String artist, String album, String year) {
        logger.info(":) - Tags found: [{}] [{}] [{}]", year, artist, album);
    }

    @Override
    public void onTagsMissing(Path directory) {
        logger.warn(":( - No tags found for: {}", directory);
    }

    @Override
    public void onStringFiltered(String original, String filtered, Path directory) {
        logger.info("String filtered: [{}] -> [{}] in {}", original, filtered, directory);
    }

    @Override
    public void onFileCopyStarted(Path source, Path destination) {
        logger.trace("Copying: {} -> {}", source.getFileName(), destination);
    }

    @Override
    public void onFileCopied(FileReport fileReport) {
        logger.trace("Copied: {}", fileReport.getSource().getFileName());
    }

    @Override
    public void onFileCopyFailed(FileReport fileReport) {
        logger.fatal(":( - COPY FAILED: {} - {}",
            fileReport.getSource(),
            fileReport.getErrorMessage());
    }

    @Override
    public void onDirectoryCompleted(DirectoryReport report) {
        logger.debug("Directory completed: {} - Status: {}, Files: {}, Copied: {}, Errors: {}",
            report.getPath(),
            report.getStatus(),
            report.getFilesCount(),
            report.getCopiedCount(),
            report.getErrorCount());
    }

    @Override
    public void onProgressUpdate(int processedDirs, int totalDirs, int processedFiles) {
        // Could be used for a progress indicator
        logger.trace("Progress: {} dirs, {} files processed", processedDirs, processedFiles);
    }

    @Override
    public void onSortCompleted(RunTotals totals) {
        logger.info("===================== RUN SUMMARY =====================");
        logger.info("Duration: {} seconds", totals.getTotalDuration().toSeconds());
        logger.info("Directories: total={} ok={} noTag={} copyError={} empty={}",
            totals.getDirectoriesTotal(),
            totals.getOkDirs(),
            totals.getNoTagDirs(),
            totals.getCopyErrorDirs(),
            totals.getEmptyDirs());
        logger.info("Files: scanned={} copied={} failed={}",
            totals.getFilesSeen(),
            totals.getFilesCopied(),
            totals.getFilesFailed());
        logger.info("Total bytes copied: {} MB", totals.getTotalBytesCopied() / (1024 * 1024));
        logger.info("-------------------------------------------------------");

        // List directories without tags
        var noTagDirs = totals.getDirectoriesWithoutTags();
        if (!noTagDirs.isEmpty()) {
            logger.warn("Directories missing tags ({}):", noTagDirs.size());
            noTagDirs.forEach(d -> logger.warn("  NO TAG: {}", d.getPath()));
            logger.info("-------------------------------------------------------");
        }

        // List failed files
        var failedFiles = totals.getFailedFiles();
        if (!failedFiles.isEmpty()) {
            logger.fatal("Failed file copies ({}):", failedFiles.size());
            failedFiles.forEach(f -> logger.fatal("  COPY ERROR: {} - {}",
                f.getSource(), f.getErrorMessage()));
            logger.info("-------------------------------------------------------");
        }

        // Per-directory detail
        logger.info("Per-directory detail (status | files | copied | errors | path)");
        totals.getDirectoryReports().stream()
            .sorted(Comparator.comparing(r -> r.getPath().toString()))
            .forEach(report -> logger.info("{} | {} | {} | {} | {}",
                String.format("%-10s", report.getStatus()),
                String.format("%4d", report.getFilesCount()),
                String.format("%4d", report.getCopiedCount()),
                String.format("%4d", report.getErrorCount()),
                report.getPath()));

        logger.info("===================== END SUMMARY =====================");
    }

    @Override
    public void onError(String message, Exception exception) {
        logger.error(message, exception);
    }

    @Override
    public void onInfo(String message) {
        logger.info(message);
    }

    @Override
    public void onWarning(String message) {
        logger.warn(message);
    }
}
