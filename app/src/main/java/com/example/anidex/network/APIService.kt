package com.example.anidex.network
import com.example.anidex.model.*
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query



interface APIService {


    @GET("/v3/top/{type}/{page}/{param}")
    fun getSeries(@Path("page") page: Int?,
                  @Path("type") type: String?,
                  @Path("param") param: String?=""): Observable<Top>?

    @GET("/v3/{type}/{id}/{request}")
    fun getMangaDetail(@Path("id") id: Int?,
                            @Path("type") type: String?,
                            @Path("request") request: String?): Observable<MangaDetail?>?

    @GET("/v3/{type}/{id}/{request}")
    fun getAnimeDetail(@Path("id") id: Int?,
                       @Path("type") type: String?,
                       @Path("request") request: String?): Observable<AnimeDetail?>?

    @GET("/v3/{type}/{id}/{request}")
    fun getCharactersDetail(@Path("id") id: Int?,
                            @Path("type") type: String?,
                            @Path("request") request: String?): Observable<Characters>?


    @GET("/v3/search/{type}")
    fun searchAnime(@Path("type") type: String?,
                    @Query("q") q: String?,
                    @Query("page") page: Int?): Observable<SearchResult>?

    companion object {
        fun createClient(): APIService{

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.jikan.moe/")
                .build()
            return retrofit.create(APIService::class.java)
        }

    }
}

