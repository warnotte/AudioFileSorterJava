# AudioFilesSorter

A Java tool to analyze and organize audio files (MP3, FLAC, OGG, WAV) based on their embedded metadata (ID3 tags).

## Features

- **Scan Mode**: Analyze your music collection and generate reports without modifying files
- **Sort Mode**: Organize files into `Artist/[Year] Album` folder structure
- **HTML Reports**: Interactive reports with charts, statistics, and data tables
- **Music Catalog**: Visual catalog with album covers (Spotify-like dark theme)
- **Duplicate Detection**: Find albums in multiple formats (MP3 + FLAC)
- **Analysis**: Identify missing tags, suspicious years, small albums, missing cover art

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

This creates an executable JAR: `target/AudioFilesSorter-0.1.0.jar`

## Usage

### Scan (analyze without copying)

Generate reports and catalog for an existing music collection:

```bash
java -jar AudioFilesSorter-0.1.0.jar scan D:\Music
```

Options:
- `-o, --output <dir>` : Output directory for reports (default: ./reports)
- `--no-open` : Don't open report in browser after generation

### Sort (organize files)

Scan and copy files to a new organized structure:

```bash
java -jar AudioFilesSorter-0.1.0.jar sort D:\Music E:\Sorted
```

Options:
- `--dry-run` : Scan only, don't actually copy files
- `-r, --reports <dir>` : Output directory for reports
- `--no-open` : Don't open report in browser

### Help

```bash
java -jar AudioFilesSorter-0.1.0.jar --help
java -jar AudioFilesSorter-0.1.0.jar scan --help
java -jar AudioFilesSorter-0.1.0.jar sort --help
```

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
| `catalog.html` | Visual music catalog with album covers |
| `report.json` | Machine-readable JSON data |

### HTML Report Features

- **Summary**: Directory and file statistics, success rate
- **Charts**: Format distribution, bitrate quality, top artists, albums by year
- **Analysis**: Small albums, suspicious years, missing cover art
- **Duplicates**: Albums found in multiple formats
- **Problems**: Missing tags, copy errors, empty directories
- **Details Table**: Searchable, sortable directory listing

### Music Catalog

- Dark-themed interface with album cover art
- Artists grouped alphabetically with quick-jump navigation
- Search/filter by artist or album name
- Click album to open directory in file explorer

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
