package com.example.anidex.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Characters(
    @Expose
    @SerializedName("characters") var characters: ArrayList<Character>
): Parcelable