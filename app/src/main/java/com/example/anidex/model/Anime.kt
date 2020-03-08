package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Anime(
    @Expose
    @SerializedName("mal_id") var malId: Int,

    @Expose
    @SerializedName("rank") var rank: Int,

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
    @SerializedName("start_date") var startDate: String,

    @Expose
    @SerializedName("end_date") var endDate: String,

    @Expose
    @SerializedName("members") var members: Int,

    @Expose
    @SerializedName("score") var score: Double
)