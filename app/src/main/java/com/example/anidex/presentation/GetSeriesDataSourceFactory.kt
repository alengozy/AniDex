package com.example.anidex.presentation

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.example.anidex.model.AnimeManga
import com.example.anidex.network.APIService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class GetSeriesDataSourceFactory(
    private val service: APIService,
    private val compositeDisposable: CompositeDisposable,
    private val type: String?,
    private val request: String?
) : DataSource.Factory<Int, AnimeManga>() {
    val mutableLiveData: MutableLiveData<AnimeDataSource> = MutableLiveData()

    override fun create(): DataSource<Int, AnimeManga> {
        val animeDataSource = AnimeDataSource(service, compositeDisposable, type, 1, request)
        mutableLiveData.postValue(animeDataSource)
        return animeDataSource
    }
}