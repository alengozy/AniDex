package com.example.anidex.network

import android.net.Uri
import com.example.anidex.model.*
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface APIService {


    @GET
    fun getSeries(
        @Url seriesUrl: String
    ): Observable<Top>?

    @GET("/v3/top/{type}/{page}/")
    fun getSeries(
        @Path("page") page: Int?,
        @Path("type") type: String?
    ): Observable<Top>?

    @GET
    fun getMangaDetail(
        @Url mangaUrl: String
    ): Observable<MangaDetail?>?

    @GET
    fun getAnimeDetail(
        @Url animeUrl: String
    ): Observable<AnimeDetail?>?

    @GET
    fun getAnimeDetailRec(
        @Url animeUrl: String
    ): AnimeDetail?

    @GET
    fun getCharactersDetail(
        @Url charDetailUrl: String
    ): Observable<Characters>?


    @GET("/v3/search/{type}")
    fun searchAnime(
        @Path("type") type: String?,
        @Query("q") q: String?,
        @Query("page") page: Int?,
        @Query("order_by") order: String?,
        @Query("sort") sort: String?
    ): Observable<SearchResult>?

    @GET("/v2/users/@me")
    fun getLoggedUserId(
        @Header("Authorization") token: String
    ): Observable<User>?

    @FormUrlEncoded
    @POST("/v1/oauth2/token")
    fun exchangeTokens(
        @Field("client_id") id: String,
        @Field("code") authCode: String,
        @Field("code_verifier") codeChallenge: String,
        @Field("grant_type") grant: String,
        @Field("redirect_uri") redirUri: Uri
    ): Observable<MALToken>

    companion object {
        fun createJikanClient(): APIService {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.jikan.moe/")
                .build()
            return retrofit.create(APIService::class.java)
        }

        fun createMALClient(): APIService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.myanimelist.net/")
                .build()
            return retrofit.create(APIService::class.java)

        }

    }
}

