package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SearchResult(
    @Expose
    @SerializedName("results") val results: List<AnimeManga>
)