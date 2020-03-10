package com.example.anidex.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.anidex.model.Anime
import com.example.anidex.model.NetworkState
import com.example.anidex.network.APIService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AnimeViewModel: ViewModel(){

    var animeList: LiveData<PagedList<Anime>>

    private val compositeDisposable = CompositeDisposable()

    private val pageSize = 44

    private val sourceFactory: AnimeDataFactory

    init {
        sourceFactory = AnimeDataFactory(APIService.createClient(), compositeDisposable)
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize)
            .setEnablePlaceholders(false)
            .build()
        animeList = LivePagedListBuilder<Int, Anime>(sourceFactory, config).build()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun refresh(){
        sourceFactory.mutableLiveData.value?.invalidate()

    }

    fun getNetworkState(): LiveData<NetworkState> = Transformations.switchMap<AnimeDataSource, NetworkState>(
        sourceFactory.mutableLiveData
    ) { it.networkState }

    fun getRefreshState(): LiveData<NetworkState> = Transformations.switchMap<AnimeDataSource, NetworkState>(
        sourceFactory.mutableLiveData
    ) { it.initialLoad }

}