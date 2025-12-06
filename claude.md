# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
mvn clean package

# Compile only (no JAR)
mvn compile
```

## CLI Usage (picocli)

```bash
# Show help
java -jar target/AudioFilesSorter-0.1.0.jar --help

# Scan a directory (generate reports/catalog without copying files)
java -jar target/AudioFilesSorter-0.1.0.jar scan D:\Music
java -jar target/AudioFilesSorter-0.1.0.jar scan D:\Music -o ./my-reports --no-open

# Sort files (scan + copy to organized structure)
java -jar target/AudioFilesSorter-0.1.0.jar sort D:\Music E:\Sorted
java -jar target/AudioFilesSorter-0.1.0.jar sort D:\Music E:\Sorted --dry-run

# Legacy entry points (still available)
java -cp target/AudioFilesSorter-0.1.0.jar io.github.warnotte.SortMP3Directory
```

## Project Overview

AudioFilesSorter is a Java utility that sorts audio files (MP3, FLAC, OGG, WAV) based on embedded metadata (ID3 tags). It recursively scans directories, extracts artist/album/year tags using jaudiotagger, and reorganizes files into a hierarchical folder structure: `[ARTIST]/[YEAR] [ALBUM]/`.

## Architecture

### Core Design (io.github.warnotte.audiosorter)

The architecture separates **scanning** from **sorting**, allowing each to be used independently:

```
┌─────────────────────────────────────────────────────────────┐
│  MusicScanner (scan + metadata extraction)                  │
│         ↓                                                   │
│  RunTotals (DirectoryReports with metadata)                 │
│         ↓                                                   │
│  ┌──────┴──────┐                                            │
│  ↓             ↓                                            │
│ MusicSorter   ReportGenerator                               │
│ (copy/move)   (HTML, catalog, JSON)                         │
│                                                              │
│ AudioSorterEngine = facade combining Scanner + Sorter       │
└─────────────────────────────────────────────────────────────┘
```

**Packages:**
- `core/` - MusicScanner, MusicSorter, AudioSorterEngine, SortConfiguration
- `model/` - DirectoryReport, FileReport, RunTotals
- `listener/` - ScanProgressListener, SortProgressListener, console implementations
- `report/` - HtmlReportGenerator, JsonReportGenerator
- `cli/` - Picocli-based CLI with scan/sort subcommands

**Key classes:**
- `MusicScanner` - Scans directories and extracts metadata (no file copying)
- `MusicSorter` - Copies/organizes files based on scan results
- `AudioSorterEngine` - Facade combining both (for backwards compatibility)
- `ScanProgressListener` / `SortProgressListener` - Observer interfaces
- `HtmlReportGenerator` - Generates report.html + catalog.html using Freemarker

**Usage - Scan only (catalog generation):**
```java
MusicScanner scanner = new MusicScanner(Path.of("D:/Music"));
scanner.addListener(new ConsoleScanListener());
RunTotals results = scanner.scan();

// Generate catalog/reports
new HtmlReportGenerator().generate(results, config, Path.of("report.html"));
```

**Usage - Full sort (scan + copy):**
```java
SortConfiguration config = new SortConfiguration(inputPath, outputPath);
AudioSorterEngine engine = new AudioSorterEngine(config);
engine.addListener(new ConsoleProgressListener());
RunTotals results = engine.execute();
```

**For future GUI:** Implement `ScanProgressListener` or `SortProgressListener` and update UI components in callback methods.

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
- **picocli 4.7.5** - CLI argument parsing with annotations
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

**Design:**
- Dark-themed Spotify-like SPA (Single Page Application) interface
- Artists grouped alphabetically with quick-jump navigation
- Album cards with cover art (from first image file in directory)
- Header with mosaic background from album covers
- Album metadata: year, format, bitrate, file count

**Features:**
- **Search/Filter** - Search by artist, album name, or track filename
  - Track matches highlighted with green border and "♪ Track match" badge
- **Format & Year filters** - Dropdown filters with active filter tags
- **Statistics** - Artists/Albums/Tracks counts update dynamically when filtering

**Audio Player (SPA):**
- Fixed bottom player bar with album cover, track info, controls
- Play/Pause, Next/Previous track navigation
- Progress bar with seek functionality
- Volume control
- Playback persists across page navigation
- **Resume playback on page reload** - Shows a resume banner if music was playing

**Technical Implementation:**
- Audio player uses HTML5 Audio API with `file://` URLs
- Playback state saved to localStorage (track, position, volume)
- `data-track-names` attribute for track filename search
- `data-tracks` attribute contains all audio file paths for album playback
- Cover images served via `file:///` protocol

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

**Completed:**
- [x] **Separate Scanner from Sorter** - Split into two independent tools
  - `MusicScanner` - Scans directories, extracts metadata
  - `MusicSorter` - Copies/organizes files based on scan results
  - `AudioSorterEngine` - Facade combining both
- [x] **Duplicate detection** - Identify duplicate albums across the collection
- [x] **Picocli CLI** - Professional CLI with subcommands (scan/sort)
- [x] **Music Catalog SPA** - Full audio playback with track navigation
- [x] **Track filename search** - Search finds albums containing matching track names
- [x] **Playback persistence** - Resume playback after page reload

**In Progress / Planned:**
- [x] **Untagged albums section** - Separate section in catalog for UNKNOWN_ARTIST albums
- [x] **Tag editor integration** - Modal with copy path, open folder, links to tagging tools
- [ ] **GUI Implementation** - Create Swing/JavaFX GUI using `ScanProgressListener` interface
- [ ] **Export problems to CSV** - Allow exporting the Problems lists for batch processing
- [ ] **Playlist support** - Parse .m3u/.pls files and copy referenced tracks
- [ ] **Undo/rollback** - Keep track of operations for potential rollback
- [ ] **Multi-threaded copying** - Parallel file copy for better performance
- [ ] **Custom output pattern** - Allow configurable output folder structure (e.g., `[Genre]/[Artist]/[Year] [Album]`)
