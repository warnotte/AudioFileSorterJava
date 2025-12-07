package io.github.warnotte.audiosorter.coverart;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extracts embedded cover art from audio files and saves them to disk.
 */
public class CoverArtExtractor {

    private static final String[] AUDIO_EXTENSIONS = {".mp3", ".flac", ".ogg", ".m4a", ".wma"};
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    private int extracted = 0;
    private int skippedNoArt = 0;
    private int skippedAlreadyExists = 0;
    private int errors = 0;

    private final List<ExtractedCover> extractedCovers = new ArrayList<>();

    static {
        // Suppress jaudiotagger logging
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    /**
     * Information about an extracted cover.
     */
    public static class ExtractedCover {
        public final Path coverPath;
        public final long sizeBytes;
        public final String format;

        public ExtractedCover(Path coverPath, long sizeBytes, String format) {
            this.coverPath = coverPath;
            this.sizeBytes = sizeBytes;
            this.format = format;
        }

        public String getSizeFormatted() {
            if (sizeBytes < 1024) return sizeBytes + " B";
            if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Result of an extraction attempt for a single directory.
     */
    public enum ExtractionResult {
        EXTRACTED,           // Cover successfully extracted and saved
        ALREADY_EXISTS,      // Cover image already exists in directory
        NO_EMBEDDED_ART,     // No embedded artwork found in audio files
        NO_AUDIO_FILES,      // No audio files in directory
        ERROR                // Error during extraction
    }

    /**
     * Extracts cover art from audio files in the given directory.
     * Only extracts if no cover image already exists.
     *
     * @param directory the directory containing audio files
     * @return the result of the extraction attempt
     */
    public ExtractionResult extractCover(Path directory) {
        if (!Files.isDirectory(directory)) {
            return ExtractionResult.ERROR;
        }

        // Check if cover already exists
        if (hasCoverImage(directory)) {
            skippedAlreadyExists++;
            return ExtractionResult.ALREADY_EXISTS;
        }

        // Find audio files
        File[] audioFiles = directory.toFile().listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            for (String ext : AUDIO_EXTENSIONS) {
                if (lower.endsWith(ext)) return true;
            }
            return false;
        });

        if (audioFiles == null || audioFiles.length == 0) {
            return ExtractionResult.NO_AUDIO_FILES;
        }

        // Try to extract from each audio file until we find artwork
        for (File audioFile : audioFiles) {
            try {
                byte[] artworkData = extractArtworkFromFile(audioFile);
                if (artworkData != null) {
                    // Determine format and save
                    String extension = detectImageFormat(artworkData);
                    Path coverPath = directory.resolve("cover" + extension);
                    Files.write(coverPath, artworkData);
                    extracted++;
                    extractedCovers.add(new ExtractedCover(coverPath, artworkData.length, extension));
                    return ExtractionResult.EXTRACTED;
                }
            } catch (Exception e) {
                // Try next file
            }
        }

        skippedNoArt++;
        return ExtractionResult.NO_EMBEDDED_ART;
    }

    /**
     * Extracts artwork bytes from an audio file.
     */
    private byte[] extractArtworkFromFile(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            if (tag == null) return null;

            List<Artwork> artworkList = tag.getArtworkList();
            if (artworkList == null || artworkList.isEmpty()) return null;

            Artwork artwork = artworkList.get(0);
            return artwork.getBinaryData();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a cover image already exists in the directory.
     */
    private boolean hasCoverImage(Path directory) {
        File[] files = directory.toFile().listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            for (String ext : IMAGE_EXTENSIONS) {
                if (lower.endsWith(ext)) return true;
            }
            return false;
        });
        return files != null && files.length > 0;
    }

    /**
     * Detects the image format from the first bytes.
     */
    private String detectImageFormat(byte[] data) {
        if (data.length < 4) return ".jpg";

        // JPEG: FF D8 FF
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return ".jpg";
        }
        // PNG: 89 50 4E 47
        if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return ".png";
        }
        // GIF: 47 49 46
        if (data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46) {
            return ".gif";
        }
        // BMP: 42 4D
        if (data[0] == 0x42 && data[1] == 0x4D) {
            return ".bmp";
        }

        return ".jpg"; // Default
    }

    // Getters for statistics
    public int getExtracted() { return extracted; }
    public int getSkippedNoArt() { return skippedNoArt; }
    public int getSkippedAlreadyExists() { return skippedAlreadyExists; }
    public int getErrors() { return errors; }
    public List<ExtractedCover> getExtractedCovers() { return extractedCovers; }

    public void resetStats() {
        extracted = 0;
        skippedNoArt = 0;
        skippedAlreadyExists = 0;
        errors = 0;
        extractedCovers.clear();
    }
}
