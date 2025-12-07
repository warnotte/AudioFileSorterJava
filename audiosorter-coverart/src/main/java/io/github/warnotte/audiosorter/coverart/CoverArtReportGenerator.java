package io.github.warnotte.audiosorter.coverart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates an HTML report for cover art extraction results.
 */
public class CoverArtReportGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void generate(CoverArtReport report, Path outputPath) throws IOException {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Cover Art Extraction Report</title>\n");
        html.append("    <style>\n");
        html.append(getStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Header
        html.append("<div class=\"header\">\n");
        html.append("    <h1>Cover Art Extraction Report</h1>\n");
        html.append("    <p class=\"subtitle\">").append(escapeHtml(report.getSourceDirectory().toString())).append("</p>\n");
        html.append("    <p class=\"date\">Generated: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("</p>\n");
        html.append("</div>\n");

        // Summary
        html.append("<div class=\"summary\">\n");
        html.append("    <div class=\"stat-card\">\n");
        html.append("        <div class=\"stat-value\">").append(report.getTotalDirectories()).append("</div>\n");
        html.append("        <div class=\"stat-label\">Directories Scanned</div>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"stat-card\">\n");
        html.append("        <div class=\"stat-value\">").append(report.getAlreadyHadCover()).append("</div>\n");
        html.append("        <div class=\"stat-label\">Already Had Cover</div>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"stat-card success\">\n");
        html.append("        <div class=\"stat-value\">").append(report.getExtractedFromTags().size()).append("</div>\n");
        html.append("        <div class=\"stat-label\">Extracted from Tags</div>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"stat-card success\">\n");
        html.append("        <div class=\"stat-value\">").append(report.getDownloadedOnline().size()).append("</div>\n");
        html.append("        <div class=\"stat-label\">Downloaded Online</div>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"stat-card warning\">\n");
        html.append("        <div class=\"stat-value\">").append(report.getMissingCovers().size()).append("</div>\n");
        html.append("        <div class=\"stat-label\">Still Missing</div>\n");
        html.append("    </div>\n");
        html.append("</div>\n");

        // Extracted from tags section
        if (!report.getExtractedFromTags().isEmpty()) {
            html.append("<div class=\"section\">\n");
            html.append("    <h2>Covers Extracted from Tags (").append(report.getExtractedFromTags().size()).append(")</h2>\n");
            html.append("    <div class=\"cover-grid\">\n");
            for (CoverArtReport.CoverResult cover : report.getExtractedFromTags()) {
                html.append(renderCoverCard(cover, report.getSourceDirectory()));
            }
            html.append("    </div>\n");
            html.append("</div>\n");
        }

        // Downloaded online section
        if (!report.getDownloadedOnline().isEmpty()) {
            html.append("<div class=\"section\">\n");
            html.append("    <h2>Downloaded from MusicBrainz (").append(report.getDownloadedOnline().size()).append(")</h2>\n");
            html.append("    <div class=\"cover-grid\">\n");
            for (CoverArtReport.CoverResult cover : report.getDownloadedOnline()) {
                html.append(renderCoverCard(cover, report.getSourceDirectory()));
            }
            html.append("    </div>\n");
            html.append("</div>\n");
        }

        // Missing covers section
        if (!report.getMissingCovers().isEmpty()) {
            html.append("<div class=\"section\">\n");
            html.append("    <h2>Still Missing (").append(report.getMissingCovers().size()).append(")</h2>\n");
            html.append("    <table class=\"missing-table\">\n");
            html.append("        <thead><tr><th>Directory</th><th>Artist</th><th>Album</th><th>Reason</th></tr></thead>\n");
            html.append("        <tbody>\n");
            for (CoverArtReport.MissingCover missing : report.getMissingCovers()) {
                String relativePath = report.getSourceDirectory().relativize(missing.getAlbumDirectory()).toString();
                html.append("        <tr>\n");
                html.append("            <td><a href=\"file:///").append(escapeHtml(missing.getAlbumDirectory().toString().replace("\\", "/"))).append("\">").append(escapeHtml(relativePath)).append("</a></td>\n");
                html.append("            <td>").append(escapeHtml(missing.getArtist() != null ? missing.getArtist() : "-")).append("</td>\n");
                html.append("            <td>").append(escapeHtml(missing.getAlbum() != null ? missing.getAlbum() : "-")).append("</td>\n");
                html.append("            <td><span class=\"reason-badge\">").append(formatReason(missing.getReason())).append("</span></td>\n");
                html.append("        </tr>\n");
            }
            html.append("        </tbody>\n");
            html.append("    </table>\n");
            html.append("</div>\n");
        }

        // No covers extracted message
        if (report.getTotalExtracted() == 0 && report.getMissingCovers().isEmpty()) {
            html.append("<div class=\"section\">\n");
            html.append("    <p class=\"all-done\">All directories already have cover images!</p>\n");
            html.append("</div>\n");
        }

        html.append("</body>\n");
        html.append("</html>\n");

        Files.writeString(outputPath, html.toString());
    }

    private String renderCoverCard(CoverArtReport.CoverResult cover, Path sourceDir) {
        String relativePath = sourceDir.relativize(cover.getAlbumDirectory()).toString();
        String coverUrl = "file:///" + cover.getCoverPath().toString().replace("\\", "/");
        String folderUrl = "file:///" + cover.getAlbumDirectory().toString().replace("\\", "/");

        StringBuilder card = new StringBuilder();
        card.append("        <div class=\"cover-card\">\n");
        card.append("            <a href=\"").append(coverUrl).append("\" target=\"_blank\">\n");
        card.append("                <img src=\"").append(coverUrl).append("\" alt=\"Cover\" loading=\"lazy\">\n");
        card.append("            </a>\n");
        card.append("            <div class=\"cover-info\">\n");
        card.append("                <div class=\"cover-artist\">").append(escapeHtml(cover.getArtist() != null ? cover.getArtist() : "Unknown")).append("</div>\n");
        card.append("                <div class=\"cover-album\">").append(escapeHtml(cover.getAlbum() != null ? cover.getAlbum() : "Unknown")).append("</div>\n");
        card.append("                <div class=\"cover-meta\">\n");
        card.append("                    <span class=\"size\">").append(cover.getSizeFormatted()).append("</span>\n");
        card.append("                    <a href=\"").append(folderUrl).append("\" class=\"folder-link\" title=\"Open folder\">📁</a>\n");
        card.append("                </div>\n");
        card.append("            </div>\n");
        card.append("        </div>\n");
        return card.toString();
    }

    private String formatReason(String reason) {
        if (reason == null) return "Unknown";
        switch (reason) {
            case "no_tags": return "No artist/album tags";
            case "not_found": return "Not found on MusicBrainz";
            case "no_embedded": return "No embedded artwork";
            default: return reason;
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    private String getStyles() {
        return """
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                background: #1a1a2e;
                color: #eee;
                padding: 20px;
                min-height: 100vh;
            }
            .header {
                text-align: center;
                padding: 30px 0;
                border-bottom: 1px solid #333;
                margin-bottom: 30px;
            }
            .header h1 {
                color: #6c5ce7;
                font-size: 2.5em;
                margin-bottom: 10px;
            }
            .subtitle { color: #888; font-size: 1.1em; }
            .date { color: #666; font-size: 0.9em; margin-top: 5px; }
            .summary {
                display: flex;
                gap: 15px;
                flex-wrap: wrap;
                justify-content: center;
                margin-bottom: 40px;
            }
            .stat-card {
                background: #252540;
                border-radius: 12px;
                padding: 20px 30px;
                text-align: center;
                min-width: 150px;
            }
            .stat-card.success { border-left: 4px solid #00b894; }
            .stat-card.warning { border-left: 4px solid #fdcb6e; }
            .stat-value { font-size: 2.5em; font-weight: bold; color: #fff; }
            .stat-label { color: #888; font-size: 0.9em; margin-top: 5px; }
            .section {
                margin-bottom: 40px;
            }
            .section h2 {
                color: #a29bfe;
                margin-bottom: 20px;
                padding-bottom: 10px;
                border-bottom: 1px solid #333;
            }
            .cover-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
                gap: 20px;
            }
            .cover-card {
                background: #252540;
                border-radius: 12px;
                overflow: hidden;
                transition: transform 0.2s, box-shadow 0.2s;
            }
            .cover-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 10px 30px rgba(108, 92, 231, 0.3);
            }
            .cover-card img {
                width: 100%;
                aspect-ratio: 1;
                object-fit: cover;
                display: block;
            }
            .cover-info {
                padding: 12px;
            }
            .cover-artist {
                font-weight: 600;
                color: #fff;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .cover-album {
                color: #888;
                font-size: 0.9em;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                margin-top: 3px;
            }
            .cover-meta {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: 8px;
            }
            .size { color: #666; font-size: 0.8em; }
            .folder-link {
                text-decoration: none;
                font-size: 1.2em;
                opacity: 0.7;
                transition: opacity 0.2s;
            }
            .folder-link:hover { opacity: 1; }
            .missing-table {
                width: 100%;
                border-collapse: collapse;
                background: #252540;
                border-radius: 12px;
                overflow: hidden;
            }
            .missing-table th, .missing-table td {
                padding: 12px 15px;
                text-align: left;
                border-bottom: 1px solid #333;
            }
            .missing-table th {
                background: #1a1a2e;
                color: #a29bfe;
                font-weight: 600;
            }
            .missing-table tr:hover { background: #2a2a4a; }
            .missing-table a { color: #74b9ff; text-decoration: none; }
            .missing-table a:hover { text-decoration: underline; }
            .reason-badge {
                background: #fdcb6e;
                color: #1a1a2e;
                padding: 3px 8px;
                border-radius: 4px;
                font-size: 0.8em;
                font-weight: 500;
            }
            .all-done {
                text-align: center;
                color: #00b894;
                font-size: 1.3em;
                padding: 40px;
            }
            """;
    }
}
