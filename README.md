# AudioFilesSorter

A Java tool to analyze and organize audio files (MP3, FLAC, OGG, WAV) based on their embedded metadata (ID3 tags).

## Features

- **Scan Mode**: Analyze your music collection and generate reports without modifying files
- **Sort Mode**: Organize files into `Artist/[Year] Album` folder structure
- **HTML Reports**: Interactive reports with charts, statistics, and data tables
- **Music Catalog**: Visual catalog with album covers (Spotify-like dark theme) with audio player
- **Duplicate Detection**: Find albums in multiple formats (MP3 + FLAC)
- **Analysis**: Identify missing tags, suspicious years, small albums, missing cover art

## Project Structure (Multi-Module Maven)

```
audiosorter/
├── audiosorter-core/    # Core library (scanner, sorter, reports)
├── audiosorter-cli/     # Command-line interface
└── audiosorter-gui/     # GUI application (Swing)
```

## Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build

```bash
git clone https://github.com/warnotte/AudioFileSorterJava.git
cd AudioFileSorterJava
mvn clean package
```

This creates:
- `audiosorter-cli/target/AudioFilesSorter-0.2.0.jar` - CLI executable JAR
- `audiosorter-gui/target/AudioFilesSorter-GUI-0.2.0.jar` - GUI executable JAR

### Build Windows Distribution (with embedded JRE)

Create a standalone Windows distribution that doesn't require Java to be installed:

```bash
mvn clean package -pl audiosorter-cli -am -Pdist
```

This creates:
- `audiosorter-cli/target/AudioFilesSorter-0.2.0-windows.zip` - Distributable ZIP (~37 MB)
- `audiosorter-cli/target/dist/AudioFilesSorter/` - App folder with `AudioFilesSorter.exe`

The distribution uses jlink to create a minimal custom JRE with only the required Java modules.

### Build Native Executable (with GraalVM)

Create a native executable with instant startup (no JRE required):

```bash
build-native.bat D:\Music\TestFolder
```

This creates `audiosorter-cli/target/AudioFilesSorter-native.exe` (~60 MB, ~20 MB with UPX compression).

**Prerequisites:** GraalVM JDK, Visual Studio Build Tools, run from "x64 Native Tools Command Prompt".

## Usage

### CLI (with Java installed)

```bash
# Show help
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar --help

# Scan a directory (generate reports/catalog without copying files)
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar scan D:\Music

# Sort files (scan + copy to organized structure)
java -jar audiosorter-cli/target/AudioFilesSorter-0.2.0.jar sort D:\Music E:\Sorted
```

### Windows Distribution (no Java required)

Extract the ZIP and run:
```bash
AudioFilesSorter.exe scan D:\Music
AudioFilesSorter.exe sort D:\Music E:\Sorted --dry-run
```

### Scan Options
- `-o, --output <dir>` : Output directory for reports (default: ./reports)
- `--no-open` : Don't open report in browser after generation

### Sort Options
- `--dry-run` : Scan only, don't actually copy files
- `-r, --reports <dir>` : Output directory for reports
- `--no-open` : Don't open report in browser

## Output Structure

Files are organized as:
```
E:\Sorted\
├── The Beatles\
│   ├── [1969] Abbey Road - [FLAC 1411 kBps 44100 kHz]\
│   │   ├── 01 - Come Together.flac
│   │   └── cover.jpg
│   └── [1967] Sgt. Pepper's - [MP3 320 kBps 44100 kHz]\
│       └── ...
└── Pink Floyd\
    └── ...
```

## Reports

Generated in the `reports/` directory:

| File | Description |
|------|-------------|
| `report.html` | Interactive HTML report with charts and statistics |
| `catalog.html` | Visual music catalog with album covers and audio player |
| `report.json` | Machine-readable JSON data |

### HTML Report Features

- **Summary**: Directory and file statistics, success rate
- **Charts**: Format distribution, bitrate quality, top artists, albums by year
- **Analysis**: Small albums, suspicious years, missing cover art
- **Duplicates**: Albums found in multiple formats
- **Problems**: Missing tags, copy errors, empty directories
- **Details Table**: Searchable, sortable directory listing

### Music Catalog

- Dark-themed Spotify-like interface with album cover art
- **Audio Player**: Play albums directly in the browser with track navigation
- Artists grouped alphabetically with quick-jump navigation
- Search/filter by artist, album name, or track filename
- Untagged albums section with tagging helpers
- Playback state persists across page reload

## Supported Formats

- MP3, FLAC, OGG, WAV, M4A, AAC, WMA

## Architecture

```
MusicScanner ──→ RunTotals ──┬──→ MusicSorter (copy/move)
                             └──→ ReportGenerator (HTML, JSON)
```

- **MusicScanner**: Scans directories and extracts metadata
- **MusicSorter**: Copies/organizes files based on scan results
- **AudioSorterEngine**: Facade combining both

## Dependencies

- [jaudiotagger](https://bitbucket.org/ijabz/jaudiotagger) - Audio metadata reading
- [picocli](https://picocli.info/) - CLI argument parsing
- [Freemarker](https://freemarker.apache.org/) - HTML templating
- [Chart.js](https://www.chartjs.org/) - Interactive charts
- [Simple-DataTables](https://github.com/fiduswriter/Simple-DataTables) - Sortable tables

## License

MIT License - see LICENSE file for details.
