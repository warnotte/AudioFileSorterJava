package io.github.warnotte.audiosorter.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Report for a single directory processing.
 */
public class DirectoryReport {

    public enum Status {
        OK,
        NO_TAG,
        COPY_ERROR,
        EMPTY,
        SKIPPED
    }

    private final Path path;
    private final int depth;
    private final List<FileReport> files = new ArrayList<>();

    private boolean tagFound = false;
    private boolean empty = false;
    private boolean hasImageFile = false;
    private String coverImagePath = null; // Path to first image file found
    private int scannedFilesCount = 0; // Number of audio files found (even in debug mode)

    // Extracted metadata
    private String artist;
    private String album;
    private String year;
    private String format;
    private Integer bitrate;
    private Integer sampleRate;
    private boolean variableBitrate;

    private Path destinationPath;

    public DirectoryReport(Path path, int depth) {
        this.path = path;
        this.depth = depth;
    }

    public Path getPath() {
        return path;
    }

    public int getDepth() {
        return depth;
    }

    public List<FileReport> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void addFile(FileReport file) {
        files.add(file);
    }

    public boolean isTagFound() {
        return tagFound;
    }

    public void setTagFound(boolean tagFound) {
        this.tagFound = tagFound;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean hasImageFile() {
        return hasImageFile;
    }

    public void setHasImageFile(boolean hasImageFile) {
        this.hasImageFile = hasImageFile;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public boolean isVariableBitrate() {
        return variableBitrate;
    }

    public void setVariableBitrate(boolean variableBitrate) {
        this.variableBitrate = variableBitrate;
    }

    public Path getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(Path destinationPath) {
        this.destinationPath = destinationPath;
    }

    public int getScannedFilesCount() {
        return scannedFilesCount;
    }

    public void setScannedFilesCount(int scannedFilesCount) {
        this.scannedFilesCount = scannedFilesCount;
    }

    public int getFilesCount() {
        // Return scanned count if set, otherwise fall back to files list size
        return scannedFilesCount > 0 ? scannedFilesCount : files.size();
    }

    public long getCopiedCount() {
        return files.stream()
            .filter(f -> f.getStatus() == FileReport.Status.COPIED)
            .count();
    }

    public long getErrorCount() {
        return files.stream()
            .filter(f -> f.getStatus() == FileReport.Status.COPY_FAILED)
            .count();
    }

    public Status getStatus() {
        if (empty) {
            return Status.EMPTY;
        }
        if (!tagFound) {
            return Status.NO_TAG;
        }
        if (getErrorCount() > 0) {
            return Status.COPY_ERROR;
        }
        return Status.OK;
    }
}
