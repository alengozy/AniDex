package com.example.anidex.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Genre(
    @Expose
    @SerializedName("name") var name: String

): Parcelable