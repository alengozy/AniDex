package com.example.anidex.network
import com.example.anidex.model.Anime
import com.example.anidex.model.AnimeDetail
import com.example.anidex.model.Top
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query



interface APIService {


    @GET("/v3/top/anime/{page}/{param}")
    fun getSeries(@Path("page") page: Int?, @Path("param") param: String? = ""): Observable<Top>?

    @GET("/v3/anime/{id}/{request}")
    fun getAnimeDetail(@Path("id") id: Int?, @Path("request") request: String): Observable<AnimeDetail?>?

    @GET("/v3/search/anime")
    fun searchAnime(@Query("q") q: String?, @Query("page") page: Int?): Observable<List<Anime>>?

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

