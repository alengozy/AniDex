package com.example.anidex.network

import com.example.anidex.model.*
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


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

    companion object {
        fun createClient(): APIService {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.jikan.moe/")
                .build()
            return retrofit.create(APIService::class.java)
        }

    }
}

