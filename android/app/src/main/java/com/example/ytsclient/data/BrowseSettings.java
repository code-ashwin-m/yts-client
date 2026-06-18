package com.example.ytsclient.data;

public class BrowseSettings {
    public static final String MODE_GRID = "grid";
    public static final String MODE_LIST = "list";

    public String baseUrl = SettingsStore.DEFAULT_BASE_URL;
    public String browseMode = MODE_GRID;
    public int gridColumns = 2;
    public int pageSize = 20;
}
