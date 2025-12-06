# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
mvn clean package

# Compile only (no JAR)
mvn compile

# Run the NEW architecture (recommended)
java -cp "target/AudioFilesSorter-0.1.0.jar" io.github.warnotte.audiosorter.cli.Main [inputDir] [outputDir] [debugMode]

# Run the legacy entry point
java -cp "target/AudioFilesSorter-0.1.0.jar" io.github.warnotte.SortMP3Directory

# Run the extension lister utility
java -cp "target/AudioFilesSorter-0.1.0.jar" io.github.warnotte.ExtensionLister
```

## Project Overview

AudioFilesSorter is a Java utility that sorts audio files (MP3, FLAC, OGG, WAV) based on embedded metadata (ID3 tags). It recursively scans directories, extracts artist/album/year tags using jaudiotagger, and reorganizes files into a hierarchical folder structure: `[ARTIST]/[YEAR] [ALBUM]/`.

## Architecture

### New Architecture (io.github.warnotte.audiosorter)

Uses Observer pattern to decouple business logic from UI, enabling future GUI support.

**Packages:**
- `core/` - Engine and configuration
- `model/` - Data classes for reports
- `listener/` - Progress listener interface and implementations
- `report/` - Report generators (HTML, JSON)
- `cli/` - Command-line entry point

**Key classes:**
- `AudioSorterEngine` - Main processing engine, fires events to listeners
- `SortConfiguration` - Externalized configuration (input/output paths, debug mode, patterns)
- `SortProgressListener` - Interface for receiving progress events (implement for CLI, GUI, etc.)
- `ConsoleProgressListener` - Log4j-based implementation for console output
- `DirectoryReport` / `FileReport` / `RunTotals` - Structured result data
- `HtmlReportGenerator` - Generates modern HTML report using Freemarker templates
- `JsonReportGenerator` - Generates JSON report for programmatic access

**Usage pattern:**
```java
SortConfiguration config = new SortConfiguration(inputPath, outputPath);
AudioSorterEngine engine = new AudioSorterEngine(config);
engine.addListener(new ConsoleProgressListener());
RunTotals results = engine.execute();

// Generate reports
new HtmlReportGenerator().generate(results, config, Path.of("report.html"));
new JsonReportGenerator().generate(results, config, Path.of("report.json"));
```

**For future GUI:** Implement `SortProgressListener` and update UI components in callback methods.

### Legacy Architecture (io.github.warnotte)

Original monolithic implementation - still functional but tightly coupled to Log4j.

**Classes:**
- `SortMP3Directory` - Main entry point with hardcoded config (lines 37-39)
- `FilenameFilter_DIR`, `FilenameFilter_FILES`, `FilenameFilter_FILES_ALL` - File filters
- `ExtensionLister` - Utility to list file extensions

## Configuration

**New architecture:** Pass `Path` objects to `SortConfiguration` constructor, or use CLI args.

**Legacy:** Hardcoded in `SortMP3Directory.java`:
```java
static String inputDirectory = "e:\\manson";
static String outputDirectory = "e:\\manson_sorted";
static boolean debugMode = false;
```

## Dependencies

- **jaudiotagger 3.0.1** - Audio file metadata reading
- **log4j-core 2.20.0** - Logging (console + HTML files in logs/)
- **freemarker 2.3.32** - HTML report template engine
- **gson 2.10.1** - JSON report generation
- **Chart.js 4.4.1** (CDN) - Interactive charts in HTML report
- **Simple-DataTables 9.0.0** (CDN) - Sortable/searchable tables in HTML report

## HTML Report Features

The HTML report (`reports/report.html`) includes:

**Summary Section:**
- Directory statistics (total, OK, missing tags, errors, empty, duplicates)
- File statistics (total files, audio files, other files, copied, failed)
- Data copied (MB) and success rate progress bar
- Link to Music Catalog page

**Collection Statistics Charts:**
- Directory Status (doughnut)
- Audio Formats distribution (doughnut)
- Bitrate Quality (bar)
- Top 30 Artists by files (horizontal bar)
- Top 30 Artists by albums (horizontal bar)
- Albums by Year (bar with tooltip showing album list)
- Files by Year (bar)

**Analysis Section (collapsible sub-sections):**
- Small Albums (≤2 files) - may be singles/EPs
- Suspicious Years (before 1900 or future)
- Missing Cover Art (no image file in directory)

**Duplicate Albums Section:**
- Detects same Artist + Album in different formats/locations
- Shows format, bitrate, file count for comparison

**Problems Panel (collapsible sub-sections):**
- Directories Without Tags - collapsed by default
- Failed File Copies - collapsed by default
- Empty Directories (no audio and no subdirs) - collapsed by default
- All paths are clickable links (file:// URLs)

**Directory Details Table:**
- Sortable and searchable with Simple-DataTables
- Columns: Status, Path, Artist, Album, Year, Files, Copied, Errors
- Clickable path links

**Templates:**
- `src/main/resources/templates/report.ftl` - Main report template
- `src/main/resources/templates/catalog.ftl` - Music catalog template
- `src/main/resources/templates/partials/styles.ftl` - CSS styles
- `src/main/resources/templates/partials/charts.ftl` - Chart.js code

## Music Catalog

A separate catalog page (`reports/catalog.html`) provides a visual music library view:

- Dark-themed Spotify-like interface
- Artists grouped alphabetically with quick-jump navigation
- Album cards with cover art (from first image file in directory)
- Album metadata: year, format, bitrate, file count
- Search/filter by artist or album name
- Click album to open directory in file explorer

## Reports

Reports are generated in the `reports/` directory:
- `reports/report.html` - Modern HTML report with statistics, tables, and charts
- `reports/catalog.html` - Visual music catalog with album covers
- `reports/report.json` - Structured JSON for programmatic access

## Logging

Log4j2 outputs to `logs/` directory (for development/debugging):
- Console output
- `logs/AudioSort.html` - Complete execution log
- `logs/AudioSort-errors.html` - Errors/warnings only

Both logs/ and reports/ directories are cleaned on each run.

## Known Issues & Technical Notes

- **Freemarker number formatting:** Use `?c` for numbers in JavaScript to avoid locale-specific formatting (e.g., `2 264` instead of `2264`)
- **JS string escaping:** Use `?js_string` for all strings embedded in JavaScript
- **Windows path characters:** `filterInvalidCharacters()` handles invalid chars and trims trailing spaces/dots
- **Empty directories:** Only directories with no audio files AND no subdirectories are marked as "empty" (parent dirs are normal)

## TODO / Future Work

- [ ] **Separate Scanner from Sorter** - Split into two independent tools:
  - `MusicScanner` - Scans directories, extracts metadata, generates reports/catalog (no file copying)
  - `MusicSorter` - Uses scanner data to copy/move files into organized structure
  - Shared: metadata extraction, report generation, catalog generation
- [ ] **GUI Implementation** - Create Swing/JavaFX GUI using `SortProgressListener` interface
- [ ] **Export problems to CSV** - Allow exporting the Problems lists for batch processing
- [ ] **Tag editor integration** - Quick edit for directories without tags
- [x] **Duplicate detection** - Identify duplicate albums across the collection (DONE)
- [ ] **Playlist support** - Parse .m3u/.pls files and copy referenced tracks
- [ ] **Preview mode** - Show what would be done without actually copying
- [ ] **Undo/rollback** - Keep track of operations for potential rollback
- [ ] **Multi-threaded copying** - Parallel file copy for better performance
- [ ] **Custom output pattern** - Allow configurable output folder structure (e.g., `[Genre]/[Artist]/[Year] [Album]`)
