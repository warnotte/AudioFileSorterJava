package io.github.warnotte.audiosorter.report;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a modern HTML report using Freemarker templates.
 */
public class HtmlReportGenerator implements ReportGenerator {

    private final Configuration freemarkerConfig;

    public HtmlReportGenerator() {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        freemarkerConfig.setClassLoaderForTemplateLoading(
            getClass().getClassLoader(), "templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfig.setLogTemplateExceptions(false);
        freemarkerConfig.setWrapUncheckedExceptions(true);
    }

    @Override
    public void generate(RunTotals totals, SortConfiguration config, Path outputPath) throws IOException {
        Map<String, Object> model = buildModel(totals, config);

        // Generate main report
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            Template template = freemarkerConfig.getTemplate("report.ftl");
            template.process(model, writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }

        // Generate catalog page
        Path catalogPath = outputPath.resolveSibling("catalog.html");
        try (Writer writer = Files.newBufferedWriter(catalogPath, StandardCharsets.UTF_8)) {
            Template template = freemarkerConfig.getTemplate("catalog.ftl");
            template.process(model, writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process catalog template", e);
        }
    }

    private Map<String, Object> buildModel(RunTotals totals, SortConfiguration config) {
        Map<String, Object> model = new HashMap<>();

        // Metadata
        model.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.put("inputDirectory", config.getInputDirectory().toString());
        model.put("outputDirectory", config.getOutputDirectory().toString());
        model.put("debugMode", config.isDebugMode());

        // Duration
        long durationSeconds = totals.getTotalDuration().toSeconds();
        model.put("durationSeconds", durationSeconds);
        model.put("durationFormatted", formatDuration(durationSeconds));

        // Summary statistics
        model.put("directoriesTotal", totals.getDirectoriesTotal());
        model.put("okDirs", totals.getOkDirs());
        model.put("noTagDirs", totals.getNoTagDirs());
        model.put("copyErrorDirs", totals.getCopyErrorDirs());
        model.put("emptyDirs", totals.getEmptyDirs());

        model.put("totalFiles", totals.getTotalFilesCount());
        model.put("audioFiles", totals.getFilesSeen());
        model.put("nonAudioFiles", totals.getNonAudioFilesCount());
        model.put("filesSeen", totals.getFilesSeen());
        model.put("filesCopied", totals.getFilesCopied());
        model.put("filesFailed", totals.getFilesFailed());
        model.put("totalBytesCopied", totals.getTotalBytesCopied());
        model.put("totalMBCopied", totals.getTotalBytesCopied() / (1024.0 * 1024.0));

        // Success rate
        long total = totals.getFilesSeen();
        double successRate = total > 0 ? (totals.getFilesCopied() * 100.0 / total) : 0;
        model.put("successRate", String.format("%.1f", successRate));

        // Directory reports sorted by path
        List<Map<String, Object>> directoryReports = totals.getDirectoryReports().stream()
            .sorted(Comparator.comparing(r -> r.getPath().toString()))
            .map(this::mapDirectoryReport)
            .toList();
        model.put("directories", directoryReports);

        // Directories without tags
        List<Map<String, Object>> noTagDirsList = totals.getDirectoriesWithoutTags().stream()
            .map(this::mapDirectoryReport)
            .toList();
        model.put("directoriesWithoutTags", noTagDirsList);

        // Empty directories
        List<Map<String, Object>> emptyDirsList = totals.getDirectoryReports().stream()
            .filter(DirectoryReport::isEmpty)
            .map(this::mapDirectoryReport)
            .toList();
        model.put("emptyDirectories", emptyDirsList);

        // Duplicate albums detection (same Artist + Album, different formats)
        Map<String, List<DirectoryReport>> albumGroups = totals.getDirectoryReports().stream()
            .filter(r -> r.getArtist() != null && r.getAlbum() != null && !r.isEmpty())
            .collect(Collectors.groupingBy(
                r -> normalizeForDuplicateCheck(r.getArtist()) + " /// " + normalizeForDuplicateCheck(r.getAlbum())
            ));
        List<Map<String, Object>> duplicateGroups = albumGroups.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(e -> {
                Map<String, Object> group = new HashMap<>();
                String[] parts = e.getKey().split(" /// ");
                group.put("artist", parts[0]);
                group.put("album", parts.length > 1 ? parts[1] : "");
                group.put("count", e.getValue().size());
                group.put("directories", e.getValue().stream()
                    .map(this::mapDirectoryReport)
                    .toList());
                return group;
            })
            .toList();
        model.put("duplicateAlbums", duplicateGroups);
        model.put("duplicateAlbumsCount", duplicateGroups.size());

        // Small albums (≤2 files) - might be singles or incomplete
        List<Map<String, Object>> smallAlbums = totals.getDirectoryReports().stream()
            .filter(r -> !r.isEmpty() && r.getFilesCount() <= 2)
            .sorted(Comparator.comparing(r -> r.getFilesCount()))
            .map(this::mapDirectoryReport)
            .toList();
        model.put("smallAlbums", smallAlbums);

        // Suspicious years (< 1900 or > current year + 1)
        int currentYear = java.time.Year.now().getValue();
        List<Map<String, Object>> suspiciousYears = totals.getDirectoryReports().stream()
            .filter(r -> !r.isEmpty() && r.getYear() != null && !r.getYear().contains("UNKNOWN"))
            .filter(r -> {
                try {
                    int year = Integer.parseInt(normalizeYear(r.getYear()));
                    return year < 1900 || year > currentYear + 1;
                } catch (NumberFormatException e) {
                    return false;
                }
            })
            .map(this::mapDirectoryReport)
            .toList();
        model.put("suspiciousYears", suspiciousYears);

        // Missing cover art (no image files in directory) - only for dirs with audio files
        List<Map<String, Object>> missingCovers = totals.getDirectoryReports().stream()
            .filter(r -> !r.isEmpty() && r.getFilesCount() > 0 && !r.hasImageFile())
            .map(this::mapDirectoryReport)
            .toList();
        model.put("missingCovers", missingCovers);

        // Failed files
        List<Map<String, Object>> failedFilesList = totals.getFailedFiles().stream()
            .map(this::mapFileReport)
            .toList();
        model.put("failedFiles", failedFilesList);

        // Statistics for charts
        model.put("chartData", buildChartData(totals));

        // Catalog data - grouped by artist, sorted
        // Only exclude if artist is exactly the default "UNKNOWN_ARTIST", not if it just contains "UNKNOWN"
        Map<String, List<Map<String, Object>>> catalogByArtist = totals.getDirectoryReports().stream()
            .filter(r -> !r.isEmpty() && r.getFilesCount() > 0)
            .filter(r -> r.getArtist() != null && !r.getArtist().equals("UNKNOWN_ARTIST"))
            .collect(Collectors.groupingBy(
                DirectoryReport::getArtist,
                TreeMap::new,
                Collectors.mapping(this::mapDirectoryReport, Collectors.toList())
            ));
        model.put("catalogByArtist", catalogByArtist);
        model.put("catalogArtistCount", catalogByArtist.size());
        model.put("catalogAlbumCount", catalogByArtist.values().stream().mapToInt(List::size).sum());

        return model;
    }

    private Map<String, Object> buildChartData(RunTotals totals) {
        Map<String, Object> chartData = new HashMap<>();

        // Format distribution (only non-empty directories with tags)
        Map<String, Long> formatCounts = totals.getDirectoryReports().stream()
            .filter(r -> r.getFormat() != null && !r.isEmpty())
            .collect(Collectors.groupingBy(
                DirectoryReport::getFormat,
                Collectors.summingLong(DirectoryReport::getFilesCount)
            ));
        chartData.put("formatLabels", new ArrayList<>(formatCounts.keySet()));
        chartData.put("formatValues", new ArrayList<>(formatCounts.values()));

        // Files by Year distribution
        Map<String, Long> yearFileCounts = totals.getDirectoryReports().stream()
            .filter(r -> !r.isEmpty())
            .collect(Collectors.groupingBy(
                r -> normalizeYear(r.getYear()),
                TreeMap::new,
                Collectors.summingLong(DirectoryReport::getFilesCount)
            ));
        chartData.put("yearLabels", new ArrayList<>(yearFileCounts.keySet()));
        chartData.put("yearValues", new ArrayList<>(yearFileCounts.values()));

        // Directories (albums) by Year distribution
        Map<String, List<String>> albumsByYear = totals.getDirectoryReports().stream()
            .filter(r -> !r.isEmpty())
            .collect(Collectors.groupingBy(
                r -> normalizeYear(r.getYear()),
                TreeMap::new,
                Collectors.mapping(
                    r -> (r.getArtist() != null ? r.getArtist() : "?") + " - " + (r.getAlbum() != null ? r.getAlbum() : "?"),
                    Collectors.toList()
                )
            ));
        chartData.put("yearDirLabels", new ArrayList<>(albumsByYear.keySet()));
        chartData.put("yearDirValues", albumsByYear.values().stream().map(List::size).collect(Collectors.toList()));
        // Limit albums per year to 50 for tooltip performance
        List<List<String>> limitedAlbums = albumsByYear.values().stream()
            .map(list -> list.size() > 50 ? list.subList(0, 50) : list)
            .collect(Collectors.toList());
        chartData.put("yearDirAlbums", limitedAlbums);

        // Top 30 artists by file count
        Map<String, Long> artistFileCounts = totals.getDirectoryReports().stream()
            .filter(r -> r.getArtist() != null && !r.getArtist().contains("UNKNOWN") && !r.isEmpty())
            .collect(Collectors.groupingBy(
                DirectoryReport::getArtist,
                Collectors.summingLong(DirectoryReport::getFilesCount)
            ));
        List<Map.Entry<String, Long>> topArtistsByFiles = artistFileCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(30)
            .toList();
        chartData.put("artistLabels", topArtistsByFiles.stream().map(Map.Entry::getKey).toList());
        chartData.put("artistValues", topArtistsByFiles.stream().map(Map.Entry::getValue).toList());

        // Top 30 artists by album/directory count
        Map<String, Long> artistAlbumCounts = totals.getDirectoryReports().stream()
            .filter(r -> r.getArtist() != null && !r.getArtist().contains("UNKNOWN") && !r.isEmpty())
            .collect(Collectors.groupingBy(
                DirectoryReport::getArtist,
                Collectors.counting()
            ));
        List<Map.Entry<String, Long>> topArtistsByAlbums = artistAlbumCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(30)
            .toList();
        chartData.put("artistAlbumLabels", topArtistsByAlbums.stream().map(Map.Entry::getKey).toList());
        chartData.put("artistAlbumValues", topArtistsByAlbums.stream().map(Map.Entry::getValue).toList());

        // Bitrate distribution
        Map<String, Long> bitrateCounts = totals.getDirectoryReports().stream()
            .filter(r -> r.getBitrate() != null && !r.isEmpty())
            .collect(Collectors.groupingBy(
                r -> categorizeBitrate(r.getBitrate()),
                Collectors.summingLong(DirectoryReport::getFilesCount)
            ));
        // Sort by bitrate category
        List<String> bitrateOrder = List.of("< 128", "128", "192", "256", "320", "Lossless");
        List<String> sortedBitrateLabels = bitrateOrder.stream()
            .filter(bitrateCounts::containsKey)
            .toList();
        List<Long> sortedBitrateValues = sortedBitrateLabels.stream()
            .map(bitrateCounts::get)
            .toList();
        chartData.put("bitrateLabels", sortedBitrateLabels);
        chartData.put("bitrateValues", sortedBitrateValues);

        return chartData;
    }

    private String normalizeForDuplicateCheck(String str) {
        if (str == null) return "";
        return str.toLowerCase()
            .replaceAll("[^a-z0-9]", "") // Keep only alphanumeric
            .trim();
    }

    private String normalizeYear(String year) {
        if (year == null || year.contains("UNKNOWN")) return "Unknown";
        // Extract first 4 digits (handles "2008/2015" or "2008-01-01")
        String normalized = year.replaceAll("[^0-9].*", "");
        if (normalized.length() >= 4) {
            return normalized.substring(0, 4);
        }
        return normalized.isEmpty() ? "Unknown" : year;
    }

    private String categorizeBitrate(Integer bitrate) {
        if (bitrate == null) return "Unknown";
        if (bitrate > 500) return "Lossless";
        if (bitrate >= 320) return "320";
        if (bitrate >= 256) return "256";
        if (bitrate >= 192) return "192";
        if (bitrate >= 128) return "128";
        return "< 128";
    }

    private Map<String, Object> mapDirectoryReport(DirectoryReport report) {
        Map<String, Object> map = new HashMap<>();
        map.put("path", report.getPath().toString());
        map.put("depth", report.getDepth());
        map.put("status", report.getStatus().name());
        map.put("statusClass", getStatusCssClass(report.getStatus()));
        map.put("filesCount", report.getFilesCount());
        map.put("copiedCount", report.getCopiedCount());
        map.put("errorCount", report.getErrorCount());
        map.put("tagFound", report.isTagFound());
        map.put("empty", report.isEmpty());
        map.put("hasImageFile", report.hasImageFile());
        map.put("coverImagePath", report.getCoverImagePath());
        map.put("firstAudioFilePath", report.getFirstAudioFilePath());
        map.put("audioFilePaths", report.getAudioFilePaths());
        map.put("artist", report.getArtist());
        map.put("album", report.getAlbum());
        map.put("year", report.getYear());
        map.put("format", report.getFormat());
        map.put("bitrate", report.getBitrate());
        map.put("sampleRate", report.getSampleRate());
        map.put("variableBitrate", report.isVariableBitrate());
        if (report.getDestinationPath() != null) {
            map.put("destinationPath", report.getDestinationPath().toString());
        }
        return map;
    }

    private Map<String, Object> mapFileReport(FileReport report) {
        Map<String, Object> map = new HashMap<>();
        map.put("source", report.getSource().toString());
        map.put("status", report.getStatus().name());
        map.put("errorMessage", report.getErrorMessage());
        map.put("sizeBytes", report.getSizeBytes());
        map.put("sizeMB", report.getSizeBytes() / (1024.0 * 1024.0));
        if (report.getDestination() != null) {
            map.put("destination", report.getDestination().toString());
        }
        return map;
    }

    private String getStatusCssClass(DirectoryReport.Status status) {
        return switch (status) {
            case OK -> "status-ok";
            case NO_TAG -> "status-warning";
            case COPY_ERROR -> "status-error";
            case EMPTY -> "status-empty";
            case SKIPPED -> "status-skipped";
        };
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return String.format("%dm %ds", seconds / 60, seconds % 60);
        } else {
            return String.format("%dh %dm %ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        }
    }

    @Override
    public String getFileExtension() {
        return "html";
    }

    @Override
    public String getDescription() {
        return "HTML Report with modern styling";
    }
}
