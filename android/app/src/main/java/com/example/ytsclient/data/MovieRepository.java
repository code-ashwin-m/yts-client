package com.example.ytsclient.data;

import android.content.Context;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieRepository {
    private static final String[] TRACKERS = {
            "udp://glotorrents.pw:6969/announce",
            "udp://tracker.opentrackr.org:1337/announce",
            "udp://torrent.gresille.org:80/announce",
            "udp://tracker.openbittorrent.com:80",
            "udp://tracker.coppersurfer.tk:6969",
            "udp://tracker.leechers-paradise.org:6969",
            "udp://p4p.arenabg.ch:1337",
            "udp://tracker.internetwarriors.net:1337"
    };

    private final SettingsStore settingsStore;
    private final BookmarkStore bookmarkStore;
    private final ImageCache imageCache;
    private final ApiService apiService = new ApiService();

    public MovieRepository(Context context) {
        Context appContext = context.getApplicationContext();
        settingsStore = new SettingsStore(appContext);
        bookmarkStore = new BookmarkStore(appContext);
        imageCache = new ImageCache(appContext);
    }

    public BrowseSettings settings() {
        return settingsStore.get();
    }

    public void saveSettings(BrowseSettings settings) {
        settingsStore.save(settings);
    }

    public MovieListResult browse(String query, String quality, String rating, String genre, int page) throws Exception {
        BrowseSettings settings = settingsStore.get();
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(settings.pageSize));
        params.put("page", String.valueOf(page));
        params.put("query_term", query == null ? "" : query);
        params.put("quality", valueOrAll(quality));
        params.put("minimum_rating", valueOrAll(rating));
        params.put("genre", valueOrAll(genre));
        return apiService.listMovies(settings.baseUrl, params);
    }

    public Movie movie(int movieId) throws Exception {
        return apiService.movieDetails(settingsStore.get().baseUrl, movieId);
    }

    public List<Movie> favoriteMovies() throws Exception {
        List<Movie> movies = new ArrayList<>();
        for (Integer id : bookmarkStore.allMovieIds()) {
            Movie movie = movie(id);
            if (movie != null && movie.id != 0) {
                movies.add(movie);
            }
        }
        return movies;
    }

    public boolean isBookmarked(int movieId) {
        return bookmarkStore.isBookmarked(movieId);
    }

    public void setBookmarked(int movieId, boolean bookmarked) {
        bookmarkStore.setBookmarked(movieId, bookmarked);
    }

    public File cachedImage(String imageUrl) throws Exception {
        return imageCache.getImage(imageUrl);
    }

    public String magnetUrl(Movie movie, Torrent torrent) throws Exception {
        String title = movie.titleLong == null || movie.titleLong.isEmpty() ? movie.title : movie.titleLong;
        StringBuilder magnet = new StringBuilder("magnet:?xt=urn:btih:");
        magnet.append(torrent.hash);
        magnet.append("&dn=");
        magnet.append(URLEncoder.encode(title + " [" + torrent.quality + "]", "UTF-8"));
        for (String tracker : TRACKERS) {
            magnet.append("&tr=");
            magnet.append(URLEncoder.encode(tracker, "UTF-8"));
        }
        return magnet.toString();
    }

    private static String valueOrAll(String value) {
        return value == null || value.trim().isEmpty() ? "all" : value;
    }
}
