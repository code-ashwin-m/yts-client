package com.example.ytsclient.data;

import com.google.gson.annotations.SerializedName;

public class Torrent {
    public String hash = "";
    public String quality = "";
    public String type = "";
    @SerializedName("video_codec")
    public String videoCodec = "";

    public String label() {
        String typeLabel = type == null ? "" : type.toUpperCase();
        String codecLabel = videoCodec == null ? "" : videoCodec.toUpperCase();
        return quality + "." + typeLabel + "." + codecLabel;
    }
}
