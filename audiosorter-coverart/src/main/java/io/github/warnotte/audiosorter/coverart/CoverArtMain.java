package io.github.warnotte.audiosorter.coverart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Standalone application to extract missing cover art from audio files.
 *
 * Usage: java -jar AudioSorter-CoverArt-0.2.0.jar [--online] <music-directory>
 */
public class CoverArtMain {

    public static void main(String[] args) {
        boolean useOnline = false;
        String directory = null;

        // Parse arguments
        for (String arg : args) {
            if (arg.equals("--online") || arg.equals("-o")) {
                useOnline = true;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                System.exit(0);
            } else if (!arg.startsWith("-")) {
                directory = arg;
            }
        }

        if (directory == null) {
            printUsage();
            System.exit(1);
        }

        Path musicDir = Paths.get(directory);
        if (!Files.isDirectory(musicDir)) {
            System.err.println("ERROR: Not a valid directory: " + musicDir);
            System.exit(1);
        }

        System.out.println();
        System.out.println("=== AudioSorter CoverArt Extractor ===");
        System.out.println("Scanning: " + musicDir);
        if (useOnline) {
            System.out.println("Online mode: ENABLED (will query MusicBrainz for missing covers)");
        }
        System.out.println();

        CoverArtExtractor extractor = new CoverArtExtractor();
        MusicBrainzFetcher fetcher = useOnline ? new MusicBrainzFetcher() : null;

        int totalDirs = 0;
        int fetchedOnline = 0;
        List<CoverArtExtractor.ExtractedCover> onlineCovers = new ArrayList<>();
        List<String> missingCovers = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(musicDir)) {
            var directories = paths
                    .filter(Files::isDirectory)
                    .filter(CoverArtMain::hasAudioFiles)
                    .toList();

            totalDirs = directories.size();
            System.out.println("Found " + totalDirs + " directories with audio files");
            System.out.println();
            System.out.println("=== Phase 1: Extracting from embedded tags ===");
            System.out.println();

            List<Path> dirsWithoutCover = new ArrayList<>();

            for (Path dir : directories) {
                CoverArtExtractor.ExtractionResult result = extractor.extractCover(dir);
                String relativePath = musicDir.relativize(dir).toString();
                if (relativePath.isEmpty()) relativePath = ".";

                switch (result) {
                    case EXTRACTED:
                        System.out.println("[OK]   " + relativePath + " -> cover extracted from tags");
                        break;
                    case ALREADY_EXISTS:
                        // Silent
                        break;
                    case NO_EMBEDDED_ART:
                        dirsWithoutCover.add(dir);
                        break;
                    case NO_AUDIO_FILES:
                    case ERROR:
                        break;
                }
            }

            // Phase 2: Online fetch if enabled
            if (useOnline && !dirsWithoutCover.isEmpty()) {
                System.out.println();
                System.out.println("=== Phase 2: Fetching from MusicBrainz ===");
                System.out.println("Directories without embedded cover: " + dirsWithoutCover.size());
                System.out.println("(Rate limited to 1 request/sec)");
                System.out.println();

                for (Path dir : dirsWithoutCover) {
                    String relativePath = musicDir.relativize(dir).toString();
                    if (relativePath.isEmpty()) relativePath = ".";

                    // Get album info from tags
                    CoverArtExtractor.AlbumInfo info = extractor.getAlbumInfo(dir);
                    if (!info.isValid()) {
                        System.out.println("[SKIP] " + relativePath + " -> no artist/album tags");
                        missingCovers.add(relativePath + " (no tags)");
                        continue;
                    }

                    System.out.print("[....] " + relativePath + " -> searching \"" + info.artist + " - " + info.album + "\"...");
                    System.out.flush();

                    Optional<Path> cover = fetcher.fetchCover(info.artist, info.album, dir);
                    if (cover.isPresent()) {
                        long size = Files.size(cover.get());
                        System.out.println("\r[OK]   " + relativePath + " -> downloaded from MusicBrainz (" + formatSize(size) + ")");
                        fetchedOnline++;
                        onlineCovers.add(new CoverArtExtractor.ExtractedCover(cover.get(), size, ".jpg"));
                    } else {
                        System.out.println("\r[MISS] " + relativePath + " -> not found on MusicBrainz");
                        missingCovers.add(relativePath + " (" + info.artist + " - " + info.album + ")");
                    }
                }
            } else if (!dirsWithoutCover.isEmpty()) {
                // Not using online, list what's missing
                for (Path dir : dirsWithoutCover) {
                    String relativePath = musicDir.relativize(dir).toString();
                    if (relativePath.isEmpty()) relativePath = ".";
                    System.out.println("[SKIP] " + relativePath + " -> no embedded cover");
                    missingCovers.add(relativePath);
                }
            }

        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        // Print summary
        System.out.println();
        System.out.println("=== Results ===");
        System.out.println("Directories scanned:    " + totalDirs);
        System.out.println("Already had cover:      " + extractor.getSkippedAlreadyExists());
        System.out.println("Extracted from tags:    " + extractor.getExtracted());
        if (useOnline) {
            System.out.println("Downloaded online:      " + fetchedOnline);
        }
        System.out.println("Still missing:          " + missingCovers.size());

        // List extracted covers from tags
        if (!extractor.getExtractedCovers().isEmpty()) {
            System.out.println();
            System.out.println("=== Covers Extracted from Tags ===");
            for (CoverArtExtractor.ExtractedCover cover : extractor.getExtractedCovers()) {
                System.out.println("  " + cover.coverPath + " (" + cover.getSizeFormatted() + ")");
            }
        }

        // List covers downloaded online
        if (!onlineCovers.isEmpty()) {
            System.out.println();
            System.out.println("=== Covers Downloaded from MusicBrainz ===");
            for (CoverArtExtractor.ExtractedCover cover : onlineCovers) {
                System.out.println("  " + cover.coverPath + " (" + cover.getSizeFormatted() + ")");
            }
        }

        // List still missing
        if (!missingCovers.isEmpty() && missingCovers.size() <= 20) {
            System.out.println();
            System.out.println("=== Still Missing Cover Art ===");
            for (String path : missingCovers) {
                System.out.println("  " + path);
            }
        }

        System.out.println();
        int totalExtracted = extractor.getExtracted() + fetchedOnline;
        if (totalExtracted > 0) {
            System.out.println("SUCCESS: Got " + totalExtracted + " cover(s)!");
        } else if (!missingCovers.isEmpty() && !useOnline) {
            System.out.println("TIP: Use --online flag to search MusicBrainz for missing covers");
        } else if (!missingCovers.isEmpty()) {
            System.out.println("Some covers could not be found. Try manual search or different tags.");
        } else {
            System.out.println("All directories already have cover images.");
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar AudioSorter-CoverArt-0.2.0.jar [options] <music-directory>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --online, -o    Search MusicBrainz for covers not found in tags");
        System.out.println("  --help, -h      Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Extract embedded covers only");
        System.out.println("  java -jar AudioSorter-CoverArt-0.2.0.jar D:\\Music");
        System.out.println();
        System.out.println("  # Also search MusicBrainz for missing covers");
        System.out.println("  java -jar AudioSorter-CoverArt-0.2.0.jar --online D:\\Music");
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Checks if a directory contains audio files (non-recursive).
     */
    private static boolean hasAudioFiles(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(f -> {
                String name = f.getFileName().toString().toLowerCase();
                return name.endsWith(".mp3") || name.endsWith(".flac") ||
                       name.endsWith(".ogg") || name.endsWith(".m4a") ||
                       name.endsWith(".wma") || name.endsWith(".wav");
            });
        } catch (IOException e) {
            return false;
        }
    }
}
