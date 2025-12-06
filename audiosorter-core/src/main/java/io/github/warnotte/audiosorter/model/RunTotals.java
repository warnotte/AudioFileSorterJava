package io.github.warnotte.audiosorter.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated totals for a complete sorting run.
 */
public class RunTotals {

    private final List<DirectoryReport> directoryReports = new ArrayList<>();
    private Instant startTime;
    private Instant endTime;
    private long totalFilesCount = 0;
    private long nonAudioFilesCount = 0;

    public void addDirectoryReport(DirectoryReport report) {
        directoryReports.add(report);
    }

    public List<DirectoryReport> getDirectoryReports() {
        return Collections.unmodifiableList(directoryReports);
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getTotalDuration() {
        if (startTime == null || endTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(startTime, endTime);
    }

    public int getDirectoriesTotal() {
        return directoryReports.size();
    }

    public long getOkDirs() {
        return directoryReports.stream()
            .filter(r -> r.getStatus() == DirectoryReport.Status.OK)
            .count();
    }

    public long getNoTagDirs() {
        return directoryReports.stream()
            .filter(r -> r.getStatus() == DirectoryReport.Status.NO_TAG)
            .count();
    }

    public long getCopyErrorDirs() {
        return directoryReports.stream()
            .filter(r -> r.getStatus() == DirectoryReport.Status.COPY_ERROR)
            .count();
    }

    public long getEmptyDirs() {
        return directoryReports.stream()
            .filter(r -> r.getStatus() == DirectoryReport.Status.EMPTY)
            .count();
    }

    public long getFilesSeen() {
        return directoryReports.stream()
            .mapToLong(DirectoryReport::getFilesCount)
            .sum();
    }

    public long getFilesCopied() {
        return directoryReports.stream()
            .mapToLong(DirectoryReport::getCopiedCount)
            .sum();
    }

    public long getFilesFailed() {
        return directoryReports.stream()
            .mapToLong(DirectoryReport::getErrorCount)
            .sum();
    }

    public long getTotalBytesCopied() {
        return directoryReports.stream()
            .flatMap(d -> d.getFiles().stream())
            .filter(f -> f.getStatus() == FileReport.Status.COPIED)
            .mapToLong(FileReport::getSizeBytes)
            .sum();
    }

    public List<DirectoryReport> getDirectoriesWithoutTags() {
        return directoryReports.stream()
            .filter(r -> r.getStatus() == DirectoryReport.Status.NO_TAG)
            .toList();
    }

    public List<FileReport> getFailedFiles() {
        return directoryReports.stream()
            .flatMap(d -> d.getFiles().stream())
            .filter(f -> f.getStatus() == FileReport.Status.COPY_FAILED)
            .toList();
    }

    public void addFileCounts(int totalFiles, int nonAudioFiles) {
        this.totalFilesCount += totalFiles;
        this.nonAudioFilesCount += nonAudioFiles;
    }

    public long getTotalFilesCount() {
        return totalFilesCount;
    }

    public long getNonAudioFilesCount() {
        return nonAudioFilesCount;
    }
}
