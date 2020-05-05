package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AnimeMangaDetail(
    @Expose
    @SerializedName("title_english") var englishtitle: String,
    
    @Expose
    @SerializedName("synopsis") var synopsis: String,

    @Expose
    @SerializedName("aired") var aired: Aired,

    @Expose
    @SerializedName("status") var status: String,

    @Expose
    @SerializedName("episodes") var episodes: Int,

    @Expose
    @SerializedName("trailer_url") var trailer: String,

    @Expose
    @SerializedName("genres")var genres: List<Genre>
)
data class Aired(
    @Expose
    @SerializedName("from") var from: String,

    @Expose
    @SerializedName("to") var to: String


)

data class Genre(
    @Expose
    @SerializedName("name") var name: String

)