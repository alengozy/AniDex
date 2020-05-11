package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AnimeManga(
    @Expose
    @SerializedName("mal_id") var malId: Int,

    @Expose
    @SerializedName("title") var title: String,

    @Expose
    @SerializedName("url") var url: String,

    @Expose
    @SerializedName("image_url") var imageUrl: String,

    @Expose
    @SerializedName("type") var type: String,

    @Expose
    @SerializedName("episodes") var episodes: Int,

    @Expose
    @SerializedName("volumes") var volumes: Int,

    @Expose
    @SerializedName("members") var members: Int,

    @Expose
    @SerializedName("score") var score: Double
)