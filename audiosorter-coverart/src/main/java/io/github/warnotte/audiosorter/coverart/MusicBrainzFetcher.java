package io.github.warnotte.audiosorter.coverart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/**
 * Fetches album cover art from MusicBrainz / Cover Art Archive.
 *
 * API flow:
 * 1. Search MusicBrainz for release by artist + album name
 * 2. Get release MBID (MusicBrainz ID)
 * 3. Fetch cover from Cover Art Archive using MBID
 */
public class MusicBrainzFetcher {

    private static final String MUSICBRAINZ_API = "https://musicbrainz.org/ws/2";
    private static final String COVERART_API = "https://coverartarchive.org";
    private static final String USER_AGENT = "AudioFilesSorter/0.2.0 (https://github.com/warnotte/AudioFileSorterJava)";

    private final HttpClient httpClient;
    private long lastRequestTime = 0;

    // MusicBrainz rate limit: 1 request per second
    private static final long RATE_LIMIT_MS = 1100;

    public MusicBrainzFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Fetches cover art for an album and saves it to the directory.
     *
     * @param artist the artist name
     * @param album the album name
     * @param targetDir the directory to save the cover image
     * @return the path to the saved cover, or empty if not found
     */
    public Optional<Path> fetchCover(String artist, String album, Path targetDir) {
        try {
            // Step 1: Search for release on MusicBrainz
            Optional<String> mbid = searchRelease(artist, album);
            if (mbid.isEmpty()) {
                return Optional.empty();
            }

            // Step 2: Fetch cover from Cover Art Archive
            return fetchCoverArt(mbid.get(), targetDir);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Searches MusicBrainz for a release and returns its MBID.
     */
    private Optional<String> searchRelease(String artist, String album) throws IOException, InterruptedException {
        rateLimitWait();

        // Build search query
        String query = String.format("release:\"%s\" AND artist:\"%s\"",
                escapeQuery(album), escapeQuery(artist));
        String url = MUSICBRAINZ_API + "/release?query=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&fmt=json&limit=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return Optional.empty();
        }

        // Parse JSON response
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray releases = json.getAsJsonArray("releases");

        if (releases == null || releases.isEmpty()) {
            return Optional.empty();
        }

        // Get the first release's ID
        JsonObject firstRelease = releases.get(0).getAsJsonObject();
        String mbid = firstRelease.get("id").getAsString();

        return Optional.of(mbid);
    }

    /**
     * Fetches cover art from Cover Art Archive using release MBID.
     */
    private Optional<Path> fetchCoverArt(String mbid, Path targetDir) throws IOException, InterruptedException {
        rateLimitWait();

        // Try to get the front cover directly
        String url = COVERART_API + "/release/" + mbid + "/front-500";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            // Try without size constraint
            url = COVERART_API + "/release/" + mbid + "/front";
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                return Optional.empty();
            }
        }

        // Determine file extension from Content-Type
        String contentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");
        String extension = contentType.contains("png") ? ".png" : ".jpg";

        // Save the image
        Path coverPath = targetDir.resolve("cover" + extension);
        try (InputStream is = response.body()) {
            Files.copy(is, coverPath);
        }

        return Optional.of(coverPath);
    }

    /**
     * Escapes special characters in MusicBrainz query.
     */
    private String escapeQuery(String query) {
        // Escape Lucene special characters
        return query.replaceAll("([+\\-!(){}\\[\\]^\"~*?:\\\\/])", "\\\\$1");
    }

    /**
     * Enforces rate limiting (1 request per second for MusicBrainz).
     */
    private void rateLimitWait() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < RATE_LIMIT_MS) {
            Thread.sleep(RATE_LIMIT_MS - elapsed);
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
