package com.example.ytsclient.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieListResult {
    public List<Movie> movies = new ArrayList<>();
    @SerializedName("movie_count")
    public int movieCount;
    @SerializedName("page_number")
    public int pageNumber;
}
