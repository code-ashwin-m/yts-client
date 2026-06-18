package com.example.ytsclient.data;

import com.google.gson.annotations.SerializedName;

public class CastMember {
    public String name = "";
    @SerializedName("character_name")
    public String characterName = "";
    @SerializedName("url_small_image")
    public String smallImageUrl = "";
}
