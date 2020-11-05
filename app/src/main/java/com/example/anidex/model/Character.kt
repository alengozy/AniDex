package com.example.anidex.model
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Character (
    @Expose
    @SerializedName("mal_id") var mal_id: Int,
    @Expose
    @SerializedName("name") var name: String,
    @Expose
    @SerializedName("image_url") var image_url: String,
    @Expose
    @SerializedName("role") var role: String

) : Parcelable

