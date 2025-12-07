package io.github.warnotte.audiosorter.coverart;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains results of a cover art extraction operation.
 */
public class CoverArtReport {

    private final Path sourceDirectory;
    private final List<CoverResult> extractedFromTags = new ArrayList<>();
    private final List<CoverResult> downloadedOnline = new ArrayList<>();
    private final List<MissingCover> missingCovers = new ArrayList<>();
    private int totalDirectories;
    private int alreadyHadCover;

    public CoverArtReport(Path sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * Represents a successfully extracted/downloaded cover.
     */
    public static class CoverResult {
        private final Path coverPath;
        private final Path albumDirectory;
        private final String artist;
        private final String album;
        private final long sizeBytes;
        private final String source; // "tags" or "musicbrainz"

        public CoverResult(Path coverPath, Path albumDirectory, String artist, String album, long sizeBytes, String source) {
            this.coverPath = coverPath;
            this.albumDirectory = albumDirectory;
            this.artist = artist;
            this.album = album;
            this.sizeBytes = sizeBytes;
            this.source = source;
        }

        public Path getCoverPath() { return coverPath; }
        public Path getAlbumDirectory() { return albumDirectory; }
        public String getArtist() { return artist; }
        public String getAlbum() { return album; }
        public long getSizeBytes() { return sizeBytes; }
        public String getSource() { return source; }

        public String getSizeFormatted() {
            if (sizeBytes < 1024) return sizeBytes + " B";
            if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Represents a directory where cover could not be found.
     */
    public static class MissingCover {
        private final Path albumDirectory;
        private final String artist;
        private final String album;
        private final String reason; // "no_tags", "not_found", "no_embedded"

        public MissingCover(Path albumDirectory, String artist, String album, String reason) {
            this.albumDirectory = albumDirectory;
            this.artist = artist;
            this.album = album;
            this.reason = reason;
        }

        public Path getAlbumDirectory() { return albumDirectory; }
        public String getArtist() { return artist; }
        public String getAlbum() { return album; }
        public String getReason() { return reason; }
    }

    // Add methods
    public void addExtractedFromTags(CoverResult result) {
        extractedFromTags.add(result);
    }

    public void addDownloadedOnline(CoverResult result) {
        downloadedOnline.add(result);
    }

    public void addMissingCover(MissingCover missing) {
        missingCovers.add(missing);
    }

    // Getters
    public Path getSourceDirectory() { return sourceDirectory; }
    public List<CoverResult> getExtractedFromTags() { return extractedFromTags; }
    public List<CoverResult> getDownloadedOnline() { return downloadedOnline; }
    public List<MissingCover> getMissingCovers() { return missingCovers; }

    public int getTotalDirectories() { return totalDirectories; }
    public void setTotalDirectories(int totalDirectories) { this.totalDirectories = totalDirectories; }

    public int getAlreadyHadCover() { return alreadyHadCover; }
    public void setAlreadyHadCover(int alreadyHadCover) { this.alreadyHadCover = alreadyHadCover; }

    public int getTotalExtracted() {
        return extractedFromTags.size() + downloadedOnline.size();
    }

    public List<CoverResult> getAllExtracted() {
        List<CoverResult> all = new ArrayList<>(extractedFromTags);
        all.addAll(downloadedOnline);
        return all;
    }
}
