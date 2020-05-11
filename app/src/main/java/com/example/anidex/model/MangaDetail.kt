package com.example.anidex.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize



data class MangaDetail(
    @Expose
    @SerializedName("title_english") var englishtitle: String,

    @Expose
    @SerializedName("synopsis") var synopsis: String,

    @Expose
    @SerializedName("published") var aired: Aired,

    @Expose
    @SerializedName("status") var status: String,

    @Expose
    @SerializedName("chapters") var chapters: Int,

    @Expose
    @SerializedName("trailer_url") var trailer: String,

    @Expose
    @SerializedName("rank") var rank: Int,

    @Expose
    @SerializedName("genres")var genres: ArrayList<Genre>

)



