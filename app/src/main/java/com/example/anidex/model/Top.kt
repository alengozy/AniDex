package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Top(
    @Expose
    @SerializedName("top") var top: List<AnimeManga>?
)