package com.example.ytsclient.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    public int id;
    public String title = "";
    @SerializedName("title_long")
    public String titleLong = "";
    public int year;
    public double rating;
    public String language = "";
    @SerializedName("like_count")
    public int likeCount;
    @SerializedName(value = "description_full", alternate = {"summary"})
    public String summary = "";
    @SerializedName("medium_cover_image")
    public String mediumCoverImage = "";
    @SerializedName(value = "background_image_original", alternate = {"background_image"})
    public String backgroundImage = "";
    public List<String> genres = new ArrayList<>();
    public List<Torrent> torrents = new ArrayList<>();
    public List<CastMember> cast = new ArrayList<>();
}
