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

class SearchViewModel : ViewModel() {

    var animeList: LiveData<PagedList<AnimeManga>>
    var nState: MutableLiveData<Event<NetworkState>> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()
    private val pageSize = 10
    val searchKey: MutableLiveData<String> = MutableLiveData()
    val type: MutableLiveData<String> = MutableLiveData("")
    val order: MutableLiveData<String> = MutableLiveData("")
    val sort: MutableLiveData<String> = MutableLiveData("")
    private val service: APIService = APIService.createClient()
    private var sourceFactory: SearchDataSourceFactory =
        SearchDataSourceFactory(service, compositeDisposable,
            searchKey.value, order.value, sort.value, type.value)

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(50)
            .setEnablePlaceholders(false)
            .build()
        animeList = Transformations.switchMap(
            QuadTrigger(
                searchKey,
                order,
                sort,
                type
            )
        ) { data ->
            sourceFactory = SearchDataSourceFactory(
                APIService.createClient(),
                compositeDisposable,
                data.first,
                data.second,
                data.third,
                data.fourth
            )
            nState =
                Transformations.switchMap(sourceFactory.mutableLiveData) { it.networkState } as MutableLiveData<Event<NetworkState>>
            LivePagedListBuilder(sourceFactory, config)
                .build()
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun refresh() {
        sourceFactory.mutableLiveData.value?.invalidate()

    }


}

