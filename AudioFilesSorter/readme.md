
# 🎵 AudioFilesSorter

A lightweight Java tool to automatically sort audio files (MP3, FLAC, etc.) based on their embedded metadata (ID3 tags and others).

---

## 🚀 Features

- 🔍 Recursively scans a source folder for audio files
- 🏷️ Extracts metadata (artist, album, title, year...) using [Jaudiotagger](https://bitbucket.org/ijabz/jaudiotagger)
- 📂 Automatically organizes files into folders like `[YEAR][ARTIST][ALBUM]`
- 📋 Full logging system:
  - Reports files with missing tags
  - Logs file copy errors
  - Displays global summary at the end
- 🧪 Debug mode to simulate the process without modifying files
- 🧹 Deletes existing logs on startup

---

## 📁 Example

### Before sorting

```
D:\mp3\Various\
├── Song1.flac
├── AlbumX\
│   └── Song2.mp3
```

### After sorting (`E:\sorted`)

```
E:\sorted\
└── [1995][The Beatles][Abbey Road]\
    ├── 01 - Come Together.mp3
    └── 02 - Something.flac
```

*Example based on real metadata from the audio files.*

---

## ⚙️ Configuration

Configuration is currently done **directly in the source code**, in the `SortMP3Directory.java` file:

```java
static String inputDirectory = "D:\\mp3\\Rock";
static String outputDirectory = "E:\\sorted";
static boolean debugMode = false;
```

- `inputDirectory` – path to the input folder
- `outputDirectory` – path to the output folder (created if it doesn’t exist)
- `debugMode = true` – activates simulation mode (no actual file operations)

---

## 🛠️ Build Instructions

This is a **Maven-based project**.

### Steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/warnotte/AudioFileSorterJava.git
   cd AudioFileSorterJava/AudioFilesSorter
   ```

2. Compile the project:
   ```bash
   mvn clean package
   ```

---

## ▶️ Run Instructions

Run the main class using:

```bash
java -cp "target/AudioFilesSorter.jar;lib/*" io.github.warnotte.SortMP3Directory
```

> ℹ️ Required libraries like `jaudiotagger` are included in the `lib/` folder.

---

## 📝 Logging

A `logs/` folder is created on each run. It contains:

- A list of files with missing metadata
- A list of files that failed to copy
- A final summary report

---

## 📦 Supported Audio Formats

Currently supported (via `jaudiotagger` and file extensions):

- `.mp3`
- `.flac`
- `.ogg`
- `.wav` (partially, depending on tagging)
- others can be added easily

---

## 📄 License

This project currently has no explicit license. You are free to modify and use it as you wish. You can add a formal license (MIT, Apache, etc.) if needed.

---

## 🙌 Contributions

Possible improvements:

- External config file support (`config.properties`, YAML, etc.)
- CLI options for input/output/debug
- Graphical user interface (JavaFX, Swing)
- More flexible naming patterns
- Genre/duration-based sorting

Pull requests and feature suggestions are welcome!

---
