package io.github.warnotte.audiosorter.core;

import io.github.warnotte.audiosorter.listener.SortProgressListener;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

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
 * Main engine for sorting audio files based on metadata.
 * This class is decoupled from any UI - use listeners for progress updates.
 */
public class AudioSorterEngine {

    private final SortConfiguration config;
    private final List<SortProgressListener> listeners = new ArrayList<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final RunTotals runTotals = new RunTotals();

    private int processedDirs = 0;
    private int processedFiles = 0;

    public AudioSorterEngine(SortConfiguration config) {
        this.config = config;
    }

    public void addListener(SortProgressListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SortProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Cancel the current sorting operation.
     */
    public void cancel() {
        cancelled.set(true);
    }

    /**
     * Check if the operation was cancelled.
     */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Execute the sorting process.
     * @return RunTotals containing the results
     */
    public RunTotals execute() {
        config.validate();
        cancelled.set(false);
        processedDirs = 0;
        processedFiles = 0;

        runTotals.setStartTime(Instant.now());
        fireOnSortStarted();

        // Create output directory if not in debug mode
        if (!config.isDebugMode()) {
            try {
                Files.createDirectories(config.getOutputDirectory());
            } catch (IOException e) {
                fireOnError("Failed to create output directory", e);
                return runTotals;
            }
        }

        // Start recursive processing
        processDirectory(config.getInputDirectory().toFile(), 0);

        runTotals.setEndTime(Instant.now());
        fireOnSortCompleted();

        return runTotals;
    }

    private void processDirectory(File directory, int depth) {
        if (cancelled.get()) {
            return;
        }

        // Skip output directory
        if (directory.toPath().startsWith(config.getOutputDirectory())) {
            return;
        }

        Path dirPath = directory.toPath();
        fireOnDirectoryEntered(dirPath, depth);

        DirectoryReport report = new DirectoryReport(dirPath, depth);
        runTotals.addDirectoryReport(report);
        processedDirs++;

        // Process subdirectories first (depth-first)
        File[] subdirs = directory.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                processDirectory(subdir, depth + 1);
            }
        }

        // Get all files and audio files in this directory
        File[] allFiles = directory.listFiles(File::isFile);
        File[] audioFiles = directory.listFiles((dir, name) -> config.isAudioFile(name));
        int totalFileCount = allFiles == null ? 0 : allFiles.length;
        int fileCount = audioFiles == null ? 0 : audioFiles.length;
        int nonAudioCount = totalFileCount - fileCount;
        int subdirCount = subdirs == null ? 0 : subdirs.length;

        // Track file counts for statistics
        runTotals.addFileCounts(totalFileCount, nonAudioCount);

        if (fileCount == 0) {
            // Only mark as truly empty if no subdirectories (parent dirs are normal)
            if (subdirCount == 0) {
                report.setEmpty(true);
                fireOnDirectoryEmpty(dirPath);
            }
            fireOnDirectoryCompleted(report);
            return;
        }

        fireOnFilesFound(dirPath, fileCount);
        report.setScannedFilesCount(fileCount);

        // Extract metadata from first file with valid tags
        Tag tag = null;
        AudioHeader audioHeader = null;

        for (File audioFile : audioFiles) {
            try {
                AudioFile af = AudioFileIO.read(audioFile);
                audioHeader = af.getAudioHeader();
                Tag t = af.getTag();
                if (t != null) {
                    tag = t;
                    break;
                }
            } catch (Exception e) {
                // Skip files that can't be read
                continue;
            }
        }

        // Extract metadata
        String artist = config.getDefaultArtist();
        String album = config.getDefaultAlbum() + " (" + directory.getName() + ")";
        String year = config.getDefaultYear();

        if (tag != null) {
            report.setTagFound(true);

            String tagYear = tag.getFirst(FieldKey.YEAR);
            if (tagYear != null && !tagYear.isEmpty()) {
                year = tagYear;
            }

            String tagAlbum = tag.getFirst(FieldKey.ALBUM);
            if (tagAlbum != null && !tagAlbum.isEmpty()) {
                album = tagAlbum;
            }

            String tagArtist = tag.getFirst(FieldKey.ARTIST);
            if (tagArtist != null && !tagArtist.isEmpty()) {
                artist = tagArtist;
            }

            fireOnTagsRead(dirPath, artist, album, year);
        } else {
            report.setTagFound(false);
            fireOnTagsMissing(dirPath);
        }

        // Store metadata in report
        report.setArtist(artist);
        report.setAlbum(album);
        report.setYear(year);

        // Extract audio format info
        if (audioHeader != null) {
            report.setFormat(audioHeader.getFormat().toUpperCase());

            String bitrateStr = audioHeader.getBitRate();
            boolean vbr = bitrateStr.contains("~");
            report.setVariableBitrate(vbr);

            try {
                int bitrate = Integer.parseInt(bitrateStr.replace("~", ""));
                report.setBitrate(bitrate);
            } catch (NumberFormatException e) {
                // Ignore
            }

            try {
                int sampleRate = Integer.parseInt(audioHeader.getSampleRate());
                report.setSampleRate(sampleRate);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Filter invalid characters
        artist = filterInvalidCharacters(artist, dirPath);
        album = filterInvalidCharacters(album, dirPath);
        year = filterInvalidCharacters(year, dirPath);

        // Build album folder name with format info
        String albumFolder = buildAlbumFolderName(album, report);

        // Determine destination path
        Path destArtistDir = config.getOutputDirectory().resolve(artist);
        Path destAlbumDir = destArtistDir.resolve("[" + year + "] " + albumFolder);
        report.setDestinationPath(destAlbumDir);

        // Copy files if not in debug mode
        if (!config.isDebugMode()) {
            try {
                Files.createDirectories(destAlbumDir);
            } catch (IOException e) {
                fireOnError("Failed to create destination directory: " + destAlbumDir, e);
            }

            copyFiles(directory, destAlbumDir, report);
        }

        fireOnProgressUpdate(processedDirs, -1, processedFiles);
        fireOnDirectoryCompleted(report);
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

    private String filterInvalidCharacters(String str, Path directory) {
        String original = str;

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

        if (!original.equals(str)) {
            fireOnStringFiltered(original, str, directory);
        }

        return str;
    }

    // Event firing methods

    private void fireOnSortStarted() {
        for (SortProgressListener l : listeners) {
            l.onSortStarted(config.getInputDirectory(), config.getOutputDirectory(), config.isDebugMode());
        }
    }

    private void fireOnDirectoryEntered(Path dir, int depth) {
        for (SortProgressListener l : listeners) {
            l.onDirectoryEntered(dir, depth);
        }
    }

    private void fireOnFilesFound(Path dir, int count) {
        for (SortProgressListener l : listeners) {
            l.onFilesFound(dir, count);
        }
    }

    private void fireOnDirectoryEmpty(Path dir) {
        for (SortProgressListener l : listeners) {
            l.onDirectoryEmpty(dir);
        }
    }

    private void fireOnTagsRead(Path dir, String artist, String album, String year) {
        for (SortProgressListener l : listeners) {
            l.onTagsRead(dir, artist, album, year);
        }
    }

    private void fireOnTagsMissing(Path dir) {
        for (SortProgressListener l : listeners) {
            l.onTagsMissing(dir);
        }
    }

    private void fireOnStringFiltered(String original, String filtered, Path dir) {
        for (SortProgressListener l : listeners) {
            l.onStringFiltered(original, filtered, dir);
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

    private void fireOnProgressUpdate(int dirs, int totalDirs, int files) {
        for (SortProgressListener l : listeners) {
            l.onProgressUpdate(dirs, totalDirs, files);
        }
    }

    private void fireOnSortCompleted() {
        for (SortProgressListener l : listeners) {
            l.onSortCompleted(runTotals);
        }
    }

    private void fireOnError(String message, Exception e) {
        for (SortProgressListener l : listeners) {
            l.onError(message, e);
        }
    }
}
