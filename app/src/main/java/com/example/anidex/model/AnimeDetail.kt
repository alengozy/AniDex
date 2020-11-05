package com.example.anidex.model


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


data class AnimeDetail(
    @Expose
    @SerializedName("title_english") var englishtitle: String,
    
    @Expose
    @SerializedName("synopsis") var synopsis: String,

    @Expose
    @SerializedName("aired") var aired: Aired,

    @Expose
    @SerializedName("published") var published: Aired,

    @Expose
    @SerializedName("status") var status: String,

    @Expose
    @SerializedName("episodes") var episodes: Int,

    @Expose
    @SerializedName("chapters") var volumes: Int,

    @Expose
    @SerializedName("trailer_url") var trailer: String,

    @Expose
    @SerializedName("rank") var rank: Int,

    @Expose
    @SerializedName("genres")var genres: ArrayList<Genre>

)



