package com.example.anidex.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.anidex.model.AnimeManga
import com.example.anidex.model.NetworkState
import com.example.anidex.network.APIService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AnimeViewModel : ViewModel() {

    var animeList: LiveData<PagedList<AnimeManga>>
    private val compositeDisposable = CompositeDisposable()
    private val pageSize = 10
    private val service: APIService = APIService.createClient()
    var type: MutableLiveData<String> = MutableLiveData("anime")
    var nState: MutableLiveData<Event<NetworkState>> = MutableLiveData()
    var request: MutableLiveData<String> = MutableLiveData("")
    private var sourceFactory: GetSeriesDataSourceFactory =
        GetSeriesDataSourceFactory(service, compositeDisposable, type.value, request.value)

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(50)
            .setEnablePlaceholders(false)
            .build()
        animeList = Transformations.switchMap(
            DoubleTrigger(
                type,
                request
            )
        ) { input ->
            sourceFactory =
                GetSeriesDataSourceFactory(service, compositeDisposable, input.first, input.second)
            nState =
                Transformations.switchMap(sourceFactory.mutableLiveData) { it.networkState } as MutableLiveData<Event<NetworkState>>
            LivePagedListBuilder(sourceFactory, config).build()
        }
    }

    fun refresh() {
        sourceFactory.mutableLiveData.value?.invalidate()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()

    }

}