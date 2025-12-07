# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure (Multi-Module Maven)

```
audiosorter/
├── pom.xml                      # Parent POM (v0.2.0)
├── audiosorter-core/            # Core library
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../audiosorter/
│       │   ├── core/            # MusicScanner, MusicSorter, Engine
│       │   ├── model/           # DirectoryReport, FileReport, RunTotals
│       │   ├── listener/        # Progress listeners
│       │   └── report/          # HTML, JSON generators
│       └── resources/templates/ # Freemarker templates
├── audiosorter-cli/             # CLI module
│   ├── pom.xml
│   └── src/main/java/.../cli/   # Picocli commands
├── audiosorter-gui/             # GUI module (JavaFX + AtlantaFX)
│   ├── pom.xml
│   └── src/main/java/.../gui/   # App, Launcher, MainController
│       └── resources/
│           └── views/main.fxml  # FXML layout
└── audiosorter-coverart/        # Cover art extraction module (experimental)
    ├── pom.xml
    └── src/main/java/.../coverart/
        ├── CoverArtExtractor.java   # Extract from embedded tags
        ├── CoverArtMain.java        # Standalone CLI
        └── MusicBrainzFetcher.java  # Fetch from Cover Art Archive
```

## Build Commands

```bash
# Build all modules
mvn clean package

# Compile only
mvn compile

# Build specific module
mvn package -pl audiosorter-cli -am

# Build Windows distribution with embedded JRE (no Java required on target)
mvn clean package -pl audiosorter-cli -am -Pdist

# Build native executable with GraalVM (smallest, fastest startup)
build-native.bat D:\Music\TestFolder      # CLI
build-native-gui.bat                       # GUI (requires user interaction during tracing)
```

### Distribution Output (with -Pdist profile)

```
audiosorter-cli/target/
├── AudioFilesSorter-0.2.0.jar              # Fat JAR (requires Java)
├── AudioFilesSorter-0.2.0-windows.zip      # Distributable ZIP (~37 MB)
└── dist/AudioFilesSorter/                  # App-image folder (~53 MB)
    ├── AudioFilesSorter.exe                # Windows executable
    ├── app/                                # Application JAR (~6 MB)
    └── runtime/                            # Custom minimal JRE (~47 MB)
```

The ZIP can be distributed to users who don't have Java installed.

**JRE Optimization:** The distribution uses jlink to create a minimal custom JRE containing only the required modules (`java.base`, `java.desktop`, `java.sql`, etc.) instead of the full JDK (~152 MB). This reduces the ZIP size from ~62 MB to ~37 MB (40% smaller).

### Native Executable (with GraalVM)

```
audiosorter-cli/target/
└── AudioFilesSorter-native.exe        # CLI native (~60 MB, ~20 MB with UPX)

audiosorter-gui/target/
└── AudioFilesSorter-GUI-native.exe    # GUI native (~80 MB, ~37 MB with UPX)
```

**Prerequisites:**
- GraalVM JDK installed (GRAALVM_HOME or in PATH)
- Visual Studio Build Tools (MSVC compiler)
- Run from "x64 Native Tools Command Prompt"

**Benefits:** Instant startup, no JRE required, single file distribution.

**GUI Native Build:** The tracing agent requires user interaction - scan a folder, change themes, then close the app.

## CLI Usage

```bash
# Show help
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar --help

# Scan a directory (generate reports/catalog without copying files)
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar scan D:\Music
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar scan D:\Music -o ./my-reports --no-open

# Sort files (scan + copy to organized structure)
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar sort D:\Music E:\Sorted
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar sort D:\Music E:\Sorted --dry-run
```

## GUI Usage

```bash
# Launch GUI
java -jar audiosorter-gui/target/AudioFilesSorter-GUI-0.2.0.jar
```

## Cover Art Extractor Usage

```bash
# Extract covers from embedded tags only
java -jar audiosorter-coverart/target/AudioSorter-CoverArt-0.2.0.jar D:\Music

# Also search MusicBrainz for missing covers (rate limited 1 req/sec)
java -jar audiosorter-coverart/target/AudioSorter-CoverArt-0.2.0.jar --online D:\Music
```

**Features:**
- Phase 1: Extract embedded artwork from MP3/FLAC tags
- Phase 2 (with `--online`): Search MusicBrainz and download from Cover Art Archive
- Skips directories that already have cover images
- Lists all extracted covers with file paths and sizes

## Project Overview

AudioFilesSorter is a Java utility that sorts audio files (MP3, FLAC, OGG, WAV) based on embedded metadata (ID3 tags). It recursively scans directories, extracts artist/album/year tags using jaudiotagger, and reorganizes files into a hierarchical folder structure: `[ARTIST]/[YEAR] [ALBUM]/`.

## Architecture

### Core Design (audiosorter-core)

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

**For GUI:** Implement `ScanProgressListener` or `SortProgressListener` and update UI components in callback methods.

### Legacy Architecture (io.github.warnotte.oldVersion)

Original monolithic implementation - kept for reference. Located in `audiosorter-core`.

## Module Dependencies

| Module | Dependencies |
|--------|-------------|
| **audiosorter-core** | jaudiotagger, freemarker, gson, log4j-core |
| **audiosorter-cli** | audiosorter-core, picocli |
| **audiosorter-gui** | audiosorter-core, javafx-controls, javafx-fxml, atlantafx-base |
| **audiosorter-coverart** | audiosorter-core, jaudiotagger, gson (for JSON parsing) |

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
- `audiosorter-core/src/main/resources/templates/report.ftl` - Main report template
- `audiosorter-core/src/main/resources/templates/catalog.ftl` - Music catalog template

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
- **Untagged Albums Section** - Separate section for UNKNOWN_ARTIST albums with tagging modal

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

## Git Commit Guidelines

- **No AI mentions in commits**: Do not include Claude, Anthropic, or AI-related mentions in commit messages or Co-Authored-By tags.

## Known Issues & Technical Notes

- **Freemarker number formatting:** Use `?c` for numbers in JavaScript to avoid locale-specific formatting (e.g., `2 264` instead of `2264`)
- **JS string escaping:** Use `?js_string` for all strings embedded in JavaScript
- **Windows path characters:** `filterInvalidCharacters()` handles invalid chars and trims trailing spaces/dots
- **Empty directories:** Only directories with no audio files AND no subdirectories are marked as "empty" (parent dirs are normal)

## TODO / Future Work

**Completed:**
- [x] **Separate Scanner from Sorter** - Split into two independent tools
- [x] **Duplicate detection** - Identify duplicate albums across the collection
- [x] **Picocli CLI** - Professional CLI with subcommands (scan/sort)
- [x] **Music Catalog SPA** - Full audio playback with track navigation
- [x] **Track filename search** - Search finds albums containing matching track names
- [x] **Playback persistence** - Resume playback after page reload
- [x] **Untagged albums section** - Separate section in catalog for UNKNOWN_ARTIST albums
- [x] **Tag editor integration** - Modal with copy path, open folder, links to tagging tools
- [x] **Multi-module Maven** - Separate core, cli, gui modules
- [x] **Windows distribution** - jpackage app-image with embedded JRE + ZIP
- [x] **Minimal JRE with jlink** - Custom JRE with only required modules (~37 MB ZIP vs ~62 MB)
- [x] **GraalVM native-image CLI** - Native executable build script (~60 MB, ~20 MB with UPX)
- [x] **JavaFX GUI** - Dark-themed GUI with scan/sort modes, progress bar, cancel support, AtlantaFX themes
- [x] **GUI Windows distribution** - jpackage app-image with embedded JRE (~45 MB ZIP)
- [x] **GraalVM native-image GUI** - Native GUI executable with tracing agent (~80 MB, ~37 MB with UPX)
- [x] **Cover Art Extractor** - Standalone module to extract covers from embedded tags + MusicBrainz API

**Planned:**
- [ ] **Integrate coverart into CLI/GUI** - Add cover extraction as command/option in main tools
- [ ] **Normalize date format** - Some albums show `[2006]` while others show `[2016-10-06]` (YYYY-MM-DD). Should extract only the year from full dates for consistent display.
- [ ] **Audio fingerprinting** - AcoustID/MusicBrainz integration for auto-tagging
- [ ] **Playlist support** - Parse .m3u/.pls files and copy referenced tracks
- [ ] **Undo/rollback** - Keep track of operations for potential rollback
- [ ] **Multi-threaded copying** - Parallel file copy for better performance
- [ ] **Custom output pattern** - Allow configurable output folder structure
