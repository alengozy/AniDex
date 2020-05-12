package com.example.anidex.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.anidex.DoubleTrigger
import com.example.anidex.Event
import com.example.anidex.model.AnimeManga
import com.example.anidex.model.NetworkState
import com.example.anidex.network.APIService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AnimeViewModel: ViewModel(){


    var animeList: LiveData<PagedList<AnimeManga>>
    private val compositeDisposable = CompositeDisposable()
    private val pageSize = 10
    var type: MutableLiveData<String> = MutableLiveData("anime")
    var nState: MutableLiveData<Event<NetworkState>> = MutableLiveData()
    var request: MutableLiveData<String> = MutableLiveData("")
    private val service: APIService = APIService.createClient()
    private var sourceFactory: GetSeriesDataSourceFactory = GetSeriesDataSourceFactory(service, compositeDisposable, type.value, request.value)
    init {
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(50)
            .setEnablePlaceholders(false)
            .build()
        animeList = Transformations.switchMap(DoubleTrigger(type, request)){ input->
            sourceFactory = GetSeriesDataSourceFactory(service, compositeDisposable, input.first, input.second)
            nState = Transformations.switchMap(sourceFactory.mutableLiveData){it.networkState} as MutableLiveData<Event<NetworkState>>
            LivePagedListBuilder(sourceFactory, config).build()
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()

    }

    fun refresh(){
        sourceFactory.mutableLiveData.value?.invalidate()

    }




}