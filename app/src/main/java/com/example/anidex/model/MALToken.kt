package com.example.anidex.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MALToken(
    @Expose
    @SerializedName("token_type") val type: String,

    @Expose
    @SerializedName("expires_in") val expires: Int,

    @Expose
    @SerializedName("access_token") val accessToken: String,

    @Expose
    @SerializedName("refresh_token") val refreshToken: String
)