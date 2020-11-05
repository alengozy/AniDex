package com.example.anidex.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Aired(
    @Expose
    @SerializedName("from") var from: String,

    @Expose
    @SerializedName("to") var to: String
):Parcelable