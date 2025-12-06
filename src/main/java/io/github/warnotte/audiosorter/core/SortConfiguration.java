package io.github.warnotte.audiosorter.core;

import java.nio.file.Path;
import java.util.Set;

/**
 * Configuration for the audio sorter engine.
 */
public class SortConfiguration {

    private Path inputDirectory;
    private Path outputDirectory;
    private boolean debugMode = false;

    // Naming pattern: {ARTIST}, {YEAR}, {ALBUM}, {FORMAT}, {BITRATE}, {SAMPLERATE}
    private String artistFolderPattern = "{ARTIST}";
    private String albumFolderPattern = "[{YEAR}] {ALBUM} - [{FORMAT} {BITRATE} kBps {SAMPLERATE} kHz]";

    // Supported extensions
    private Set<String> audioExtensions = Set.of("mp3", "flac", "ogg", "wav", "m4a", "aac", "wma");

    // Default values for missing tags
    private String defaultArtist = "UNKNOWN_ARTIST";
    private String defaultAlbum = "UNKNOWN_ALBUM";
    private String defaultYear = "UNKNOWN_YEAR";

    public SortConfiguration() {
    }

    public SortConfiguration(Path inputDirectory, Path outputDirectory) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
    }

    public Path getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(Path inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public String getArtistFolderPattern() {
        return artistFolderPattern;
    }

    public void setArtistFolderPattern(String artistFolderPattern) {
        this.artistFolderPattern = artistFolderPattern;
    }

    public String getAlbumFolderPattern() {
        return albumFolderPattern;
    }

    public void setAlbumFolderPattern(String albumFolderPattern) {
        this.albumFolderPattern = albumFolderPattern;
    }

    public Set<String> getAudioExtensions() {
        return audioExtensions;
    }

    public void setAudioExtensions(Set<String> audioExtensions) {
        this.audioExtensions = audioExtensions;
    }

    public String getDefaultArtist() {
        return defaultArtist;
    }

    public void setDefaultArtist(String defaultArtist) {
        this.defaultArtist = defaultArtist;
    }

    public String getDefaultAlbum() {
        return defaultAlbum;
    }

    public void setDefaultAlbum(String defaultAlbum) {
        this.defaultAlbum = defaultAlbum;
    }

    public String getDefaultYear() {
        return defaultYear;
    }

    public void setDefaultYear(String defaultYear) {
        this.defaultYear = defaultYear;
    }

    public boolean isAudioFile(String filename) {
        String lower = filename.toLowerCase();
        return audioExtensions.stream().anyMatch(ext -> lower.endsWith("." + ext));
    }

    /**
     * Validates the configuration.
     * @throws IllegalStateException if configuration is invalid
     */
    public void validate() {
        if (inputDirectory == null) {
            throw new IllegalStateException("Input directory is required");
        }
        if (outputDirectory == null) {
            throw new IllegalStateException("Output directory is required");
        }
        if (!java.nio.file.Files.isDirectory(inputDirectory)) {
            throw new IllegalStateException("Input directory does not exist: " + inputDirectory);
        }
    }
}
