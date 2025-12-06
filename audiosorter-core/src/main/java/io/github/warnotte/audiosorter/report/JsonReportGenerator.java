package io.github.warnotte.audiosorter.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.model.DirectoryReport;
import io.github.warnotte.audiosorter.model.FileReport;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a JSON report for programmatic access.
 */
public class JsonReportGenerator implements ReportGenerator {

    private final Gson gson;

    public JsonReportGenerator() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    }

    @Override
    public void generate(RunTotals totals, SortConfiguration config, Path outputPath) throws IOException {
        Map<String, Object> report = buildReport(totals, config);
        String json = gson.toJson(report);
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);
    }

    private Map<String, Object> buildReport(RunTotals totals, SortConfiguration config) {
        // Using LinkedHashMap to preserve insertion order
        Map<String, Object> report = new LinkedHashMap<>();

        // Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("inputDirectory", config.getInputDirectory().toString());
        metadata.put("outputDirectory", config.getOutputDirectory().toString());
        metadata.put("debugMode", config.isDebugMode());
        report.put("metadata", metadata);

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("durationSeconds", totals.getTotalDuration().toSeconds());

        Map<String, Object> directories = new LinkedHashMap<>();
        directories.put("total", totals.getDirectoriesTotal());
        directories.put("ok", totals.getOkDirs());
        directories.put("noTag", totals.getNoTagDirs());
        directories.put("copyError", totals.getCopyErrorDirs());
        directories.put("empty", totals.getEmptyDirs());
        summary.put("directories", directories);

        Map<String, Object> files = new LinkedHashMap<>();
        files.put("seen", totals.getFilesSeen());
        files.put("copied", totals.getFilesCopied());
        files.put("failed", totals.getFilesFailed());
        files.put("totalBytes", totals.getTotalBytesCopied());
        summary.put("files", files);

        report.put("summary", summary);

        // Directory details
        List<Map<String, Object>> directoryDetails = totals.getDirectoryReports().stream()
            .sorted(Comparator.comparing(r -> r.getPath().toString()))
            .map(this::mapDirectoryReport)
            .toList();
        report.put("directoryDetails", directoryDetails);

        // Problems
        Map<String, Object> problems = new LinkedHashMap<>();

        List<String> noTagPaths = totals.getDirectoriesWithoutTags().stream()
            .map(d -> d.getPath().toString())
            .toList();
        problems.put("directoriesWithoutTags", noTagPaths);

        List<Map<String, Object>> failedFiles = totals.getFailedFiles().stream()
            .map(this::mapFileReport)
            .toList();
        problems.put("failedFiles", failedFiles);

        report.put("problems", problems);

        return report;
    }

    private Map<String, Object> mapDirectoryReport(DirectoryReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", report.getPath().toString());
        map.put("depth", report.getDepth());
        map.put("status", report.getStatus().name());
        map.put("filesCount", report.getFilesCount());
        map.put("copiedCount", report.getCopiedCount());
        map.put("errorCount", report.getErrorCount());

        if (report.isTagFound()) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("artist", report.getArtist());
            metadata.put("album", report.getAlbum());
            metadata.put("year", report.getYear());
            if (report.getFormat() != null) {
                metadata.put("format", report.getFormat());
                metadata.put("bitrate", report.getBitrate());
                metadata.put("sampleRate", report.getSampleRate());
                metadata.put("variableBitrate", report.isVariableBitrate());
            }
            map.put("metadata", metadata);
        }

        if (report.getDestinationPath() != null) {
            map.put("destinationPath", report.getDestinationPath().toString());
        }

        return map;
    }

    private Map<String, Object> mapFileReport(FileReport report) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("source", report.getSource().toString());
        map.put("status", report.getStatus().name());
        map.put("errorMessage", report.getErrorMessage());
        map.put("sizeBytes", report.getSizeBytes());
        if (report.getDestination() != null) {
            map.put("destination", report.getDestination().toString());
        }
        return map;
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    @Override
    public String getDescription() {
        return "JSON Report for programmatic access";
    }
}
