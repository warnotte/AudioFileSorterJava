package io.github.warnotte.audiosorter.coverart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Standalone application to extract missing cover art from audio files.
 *
 * Usage: java -jar AudioSorter-CoverArt-0.2.0.jar <music-directory>
 */
public class CoverArtMain {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar AudioSorter-CoverArt-0.2.0.jar <music-directory>");
            System.out.println();
            System.out.println("Scans the directory recursively and extracts embedded cover art");
            System.out.println("from audio files where no cover image exists.");
            System.exit(1);
        }

        Path musicDir = Paths.get(args[0]);
        if (!Files.isDirectory(musicDir)) {
            System.err.println("ERROR: Not a valid directory: " + musicDir);
            System.exit(1);
        }

        System.out.println();
        System.out.println("=== AudioSorter CoverArt Extractor ===");
        System.out.println("Scanning: " + musicDir);
        System.out.println();

        CoverArtExtractor extractor = new CoverArtExtractor();
        int totalDirs = 0;

        try (Stream<Path> paths = Files.walk(musicDir)) {
            var directories = paths
                    .filter(Files::isDirectory)
                    .filter(CoverArtMain::hasAudioFiles)
                    .toList();

            totalDirs = directories.size();
            System.out.println("Found " + totalDirs + " directories with audio files");
            System.out.println();
            System.out.println("Extracting covers from embedded tags...");
            System.out.println();

            for (Path dir : directories) {
                CoverArtExtractor.ExtractionResult result = extractor.extractCover(dir);
                String relativePath = musicDir.relativize(dir).toString();
                if (relativePath.isEmpty()) relativePath = ".";

                switch (result) {
                    case EXTRACTED:
                        System.out.println("[OK]   " + relativePath + " -> cover extracted");
                        break;
                    case ALREADY_EXISTS:
                        // Silent - don't clutter output
                        break;
                    case NO_EMBEDDED_ART:
                        System.out.println("[SKIP] " + relativePath + " -> no embedded cover");
                        break;
                    case NO_AUDIO_FILES:
                        // Silent
                        break;
                    case ERROR:
                        System.out.println("[ERR]  " + relativePath + " -> error");
                        break;
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
        System.out.println("Covers extracted:       " + extractor.getExtracted());
        System.out.println("Already had cover:      " + extractor.getSkippedAlreadyExists());
        System.out.println("No embedded artwork:    " + extractor.getSkippedNoArt());

        // List all extracted covers
        if (!extractor.getExtractedCovers().isEmpty()) {
            System.out.println();
            System.out.println("=== Extracted Covers ===");
            for (CoverArtExtractor.ExtractedCover cover : extractor.getExtractedCovers()) {
                System.out.println("  " + cover.coverPath + " (" + cover.getSizeFormatted() + ")");
            }
        }

        System.out.println();
        if (extractor.getExtracted() > 0) {
            System.out.println("SUCCESS: Extracted " + extractor.getExtracted() + " cover(s)!");
        } else if (extractor.getSkippedNoArt() > 0) {
            System.out.println("No embedded covers found. Consider using MusicBrainz API (future feature).");
        } else {
            System.out.println("All directories already have cover images.");
        }
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
