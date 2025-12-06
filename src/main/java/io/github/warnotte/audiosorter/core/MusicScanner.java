package io.github.warnotte.audiosorter.core;

import io.github.warnotte.audiosorter.listener.ScanProgressListener;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scans directories for audio files and extracts metadata.
 * This is the pure scanning component - no file copying.
 * Use this for catalog generation or analysis without modifying files.
 */
public class MusicScanner {

    private final Path inputDirectory;
    private final Set<String> audioExtensions;
    private final String defaultArtist;
    private final String defaultAlbum;
    private final String defaultYear;

    private final List<ScanProgressListener> listeners = new ArrayList<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final RunTotals runTotals = new RunTotals();

    private int processedDirs = 0;

    /**
     * Create a scanner with default settings.
     */
    public MusicScanner(Path inputDirectory) {
        this(inputDirectory,
             Set.of("mp3", "flac", "ogg", "wav", "m4a", "aac", "wma"),
             "UNKNOWN_ARTIST", "UNKNOWN_ALBUM", "UNKNOWN_YEAR");
    }

    /**
     * Create a scanner from existing SortConfiguration (for compatibility).
     */
    public MusicScanner(SortConfiguration config) {
        this(config.getInputDirectory(),
             config.getAudioExtensions(),
             config.getDefaultArtist(),
             config.getDefaultAlbum(),
             config.getDefaultYear());
    }

    /**
     * Create a scanner with custom settings.
     */
    public MusicScanner(Path inputDirectory, Set<String> audioExtensions,
                        String defaultArtist, String defaultAlbum, String defaultYear) {
        this.inputDirectory = inputDirectory;
        this.audioExtensions = audioExtensions;
        this.defaultArtist = defaultArtist;
        this.defaultAlbum = defaultAlbum;
        this.defaultYear = defaultYear;
    }

    public void addListener(ScanProgressListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ScanProgressListener listener) {
        listeners.remove(listener);
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public Path getInputDirectory() {
        return inputDirectory;
    }

    /**
     * Execute the scan.
     * @return RunTotals containing the scan results
     */
    public RunTotals scan() {
        if (inputDirectory == null) {
            throw new IllegalStateException("Input directory is required");
        }
        if (!java.nio.file.Files.isDirectory(inputDirectory)) {
            throw new IllegalStateException("Input directory does not exist: " + inputDirectory);
        }

        cancelled.set(false);
        processedDirs = 0;

        runTotals.setStartTime(Instant.now());
        fireOnScanStarted();

        // Start recursive scanning
        scanDirectory(inputDirectory.toFile(), 0);

        runTotals.setEndTime(Instant.now());
        fireOnScanCompleted();

        return runTotals;
    }

    private void scanDirectory(File directory, int depth) {
        if (cancelled.get()) {
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
                scanDirectory(subdir, depth + 1);
            }
        }

        // Get all files and audio files in this directory
        File[] allFiles = directory.listFiles(File::isFile);
        File[] audioFiles = directory.listFiles((dir, name) -> isAudioFile(name));
        File[] imageFiles = directory.listFiles((dir, name) -> isImageFile(name));
        int totalFileCount = allFiles == null ? 0 : allFiles.length;
        int fileCount = audioFiles == null ? 0 : audioFiles.length;
        int nonAudioCount = totalFileCount - fileCount;
        int subdirCount = subdirs == null ? 0 : subdirs.length;

        // Track file counts for statistics
        runTotals.addFileCounts(totalFileCount, nonAudioCount);

        // Check for cover art
        if (imageFiles != null && imageFiles.length > 0) {
            report.setHasImageFile(true);
            report.setCoverImagePath(imageFiles[0].getAbsolutePath());
        }

        // Store audio file paths for playback
        if (audioFiles != null) {
            for (File audioFile : audioFiles) {
                report.addAudioFilePath(audioFile.getAbsolutePath());
            }
        }

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
        String artist = defaultArtist;
        String album = defaultAlbum + " (" + directory.getName() + ")";
        String year = defaultYear;

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

        fireOnProgressUpdate(processedDirs, -1);
        fireOnDirectoryCompleted(report);
    }

    private boolean isAudioFile(String filename) {
        String lower = filename.toLowerCase();
        return audioExtensions.stream().anyMatch(ext -> lower.endsWith("." + ext));
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
               lower.endsWith(".png") || lower.endsWith(".gif") ||
               lower.endsWith(".bmp") || lower.endsWith(".webp");
    }

    // Event firing methods

    private void fireOnScanStarted() {
        for (ScanProgressListener l : listeners) {
            l.onScanStarted(inputDirectory);
        }
    }

    private void fireOnDirectoryEntered(Path dir, int depth) {
        for (ScanProgressListener l : listeners) {
            l.onDirectoryEntered(dir, depth);
        }
    }

    private void fireOnFilesFound(Path dir, int count) {
        for (ScanProgressListener l : listeners) {
            l.onFilesFound(dir, count);
        }
    }

    private void fireOnDirectoryEmpty(Path dir) {
        for (ScanProgressListener l : listeners) {
            l.onDirectoryEmpty(dir);
        }
    }

    private void fireOnTagsRead(Path dir, String artist, String album, String year) {
        for (ScanProgressListener l : listeners) {
            l.onTagsRead(dir, artist, album, year);
        }
    }

    private void fireOnTagsMissing(Path dir) {
        for (ScanProgressListener l : listeners) {
            l.onTagsMissing(dir);
        }
    }

    private void fireOnDirectoryCompleted(DirectoryReport report) {
        for (ScanProgressListener l : listeners) {
            l.onDirectoryCompleted(report);
        }
    }

    private void fireOnProgressUpdate(int dirs, int totalDirs) {
        for (ScanProgressListener l : listeners) {
            l.onProgressUpdate(dirs, totalDirs);
        }
    }

    private void fireOnScanCompleted() {
        for (ScanProgressListener l : listeners) {
            l.onScanCompleted(runTotals);
        }
    }

    private void fireOnError(String message, Exception e) {
        for (ScanProgressListener l : listeners) {
            l.onError(message, e);
        }
    }
}
