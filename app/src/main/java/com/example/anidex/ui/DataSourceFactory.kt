package com.example.anidex.ui

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.example.anidex.model.Anime
import com.example.anidex.network.APIService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AnimeDataFactory(private val service: APIService,  private val compositeDisposable: CompositeDisposable): DataSource.Factory<Int, Anime>(){
    val mutableLiveData: MutableLiveData<AnimeDataSource> = MutableLiveData()


    override fun create(): DataSource<Int, Anime> {
        val animeDataSource = AnimeDataSource(service, compositeDisposable)
        mutableLiveData.postValue(animeDataSource)
        return animeDataSource
    }
}