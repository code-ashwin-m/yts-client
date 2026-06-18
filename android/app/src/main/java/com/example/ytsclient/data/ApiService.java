package com.example.ytsclient.data;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class ApiService {
    private final Gson gson = new Gson();

    public MovieListResult listMovies(String baseUrl, Map<String, String> params) throws Exception {
        ListMoviesResponse response = gson.fromJson(get(baseUrl, "/list_movies.json", params), ListMoviesResponse.class);
        return normalizeListResult(response != null && response.data != null ? response.data : new MovieListResult());
    }

    public Movie movieDetails(String baseUrl, int movieId) throws Exception {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("movie_id", String.valueOf(movieId));
        params.put("with_images", "true");
        params.put("with_cast", "true");
        MovieDetailsResponse response = gson.fromJson(get(baseUrl, "/movie_details.json", params), MovieDetailsResponse.class);
        return response != null && response.data != null ? normalizeMovie(response.data.movie) : null;
    }

    private static String get(String baseUrl, String path, Map<String, String> params) throws Exception {
        StringBuilder url = new StringBuilder(stripSlash(baseUrl)).append(path);
        if (params != null && !params.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                    continue;
                }
                if (!first) {
                    url.append("&");
                }
                first = false;
                url.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                url.append("=");
                url.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url.toString()).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "YTSClientAndroid/1.0");

        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAll(stream);
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Request failed with HTTP " + status + ": " + body);
            }
            return body;
        } finally {
            connection.disconnect();
        }
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static String stripSlash(String value) {
        String clean = value == null || value.trim().isEmpty()
                ? SettingsStore.DEFAULT_BASE_URL
                : value.trim();
        while (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean;
    }

    private static MovieListResult normalizeListResult(MovieListResult result) {
        if (result.movies == null) {
            result.movies = new ArrayList<>();
        }
        for (Movie movie : result.movies) {
            normalizeMovie(movie);
        }
        return result;
    }

    private static Movie normalizeMovie(Movie movie) {
        if (movie == null) {
            return null;
        }
        if (movie.title == null) movie.title = "";
        if (movie.titleLong == null || movie.titleLong.isEmpty()) movie.titleLong = movie.title;
        if (movie.language == null) movie.language = "";
        if (movie.summary == null) movie.summary = "";
        if (movie.mediumCoverImage == null) movie.mediumCoverImage = "";
        if (movie.backgroundImage == null) movie.backgroundImage = "";
        if (movie.genres == null) movie.genres = new ArrayList<>();
        if (movie.torrents == null) movie.torrents = new ArrayList<>();
        if (movie.cast == null) movie.cast = new ArrayList<>();
        for (Torrent torrent : movie.torrents) {
            if (torrent == null) continue;
            if (torrent.hash == null) torrent.hash = "";
            if (torrent.quality == null) torrent.quality = "";
            if (torrent.type == null) torrent.type = "";
            if (torrent.videoCodec == null) torrent.videoCodec = "";
        }
        for (CastMember member : movie.cast) {
            if (member == null) continue;
            if (member.name == null) member.name = "";
            if (member.characterName == null) member.characterName = "";
            if (member.smallImageUrl == null) member.smallImageUrl = "";
        }
        return movie;
    }

    private static class ListMoviesResponse {
        MovieListResult data;
    }

    private static class MovieDetailsResponse {
        MovieDetailsData data;
    }

    private static class MovieDetailsData {
        @SerializedName("movie")
        Movie movie;
    }
}
