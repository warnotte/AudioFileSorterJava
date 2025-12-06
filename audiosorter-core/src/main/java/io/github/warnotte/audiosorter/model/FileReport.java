package io.github.warnotte.audiosorter.model;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Report for a single file operation.
 */
public class FileReport {

    public enum Status {
        PENDING,
        COPIED,
        COPY_FAILED,
        SKIPPED
    }

    private final Path source;
    private Path destination;
    private Status status = Status.PENDING;
    private String errorMessage;
    private long sizeBytes;
    private Duration copyDuration;

    public FileReport(Path source) {
        this.source = source;
        try {
            this.sizeBytes = java.nio.file.Files.size(source);
        } catch (Exception e) {
            this.sizeBytes = 0;
        }
    }

    public Path getSource() {
        return source;
    }

    public Path getDestination() {
        return destination;
    }

    public void setDestination(Path destination) {
        this.destination = destination;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public Duration getCopyDuration() {
        return copyDuration;
    }

    public void setCopyDuration(Duration copyDuration) {
        this.copyDuration = copyDuration;
    }

    public void markCopied(Path destination, Duration duration) {
        this.destination = destination;
        this.status = Status.COPIED;
        this.copyDuration = duration;
    }

    public void markFailed(String error) {
        this.status = Status.COPY_FAILED;
        this.errorMessage = error;
    }
}
