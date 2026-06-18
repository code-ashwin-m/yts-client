package com.example.ytsclient.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStore {
    public static final String DEFAULT_BASE_URL = "https://movies-api.accel.li/api/v2";

    private static final String PREFS = "yts_settings";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_BROWSE_MODE = "browse_mode";
    private static final String KEY_GRID_COLUMNS = "grid_columns";
    private static final String KEY_PAGE_SIZE = "page_size";

    private final SharedPreferences prefs;

    public SettingsStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public BrowseSettings get() {
        BrowseSettings settings = new BrowseSettings();
        settings.baseUrl = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        settings.browseMode = prefs.getString(KEY_BROWSE_MODE, BrowseSettings.MODE_GRID);
        settings.gridColumns = clamp(prefs.getInt(KEY_GRID_COLUMNS, 2), 1, 5);
        settings.pageSize = clamp(prefs.getInt(KEY_PAGE_SIZE, 20), 10, 50);
        return settings;
    }

    public void save(BrowseSettings settings) {
        prefs.edit()
                .putString(KEY_BASE_URL, sanitizeBaseUrl(settings.baseUrl))
                .putString(KEY_BROWSE_MODE, settings.browseMode)
                .putInt(KEY_GRID_COLUMNS, clamp(settings.gridColumns, 1, 5))
                .putInt(KEY_PAGE_SIZE, clamp(settings.pageSize, 10, 50))
                .apply();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String sanitizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return DEFAULT_BASE_URL;
        }
        String clean = baseUrl.trim();
        while (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean;
    }
}
