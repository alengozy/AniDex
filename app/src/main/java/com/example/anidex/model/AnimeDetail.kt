package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AnimeDetail(
    @Expose
    @SerializedName("synopsis") var synopsis: String


)