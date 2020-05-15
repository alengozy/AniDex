package com.example.anidex.presentation

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.example.anidex.model.AnimeManga
import com.example.anidex.model.NetworkState
import com.example.anidex.network.APIService

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class SearchDataSource(
    private val service: APIService,
    private val compositeDisposable: CompositeDisposable,
    private val searchKey: String?,
    private val type: String?,
    private val order: String?,
    private val sort: String?,
    private var page: Int
) : PageKeyedDataSource<Int, AnimeManga>() {
    val networkState = MutableLiveData<Event<NetworkState>>()
    private val initialLoad = MutableLiveData<Event<NetworkState>>()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, AnimeManga>

    ) {

        networkState.postValue(Event(NetworkState.LOADING))
        initialLoad.postValue(Event(NetworkState.LOADING))
        compositeDisposable.add(
            service.searchAnime(type, searchKey, page, order, sort)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ response ->
                    initialLoad.postValue(
                        Event(
                            NetworkState.LOADED
                        )
                    )
                    networkState.postValue(
                        Event(
                            NetworkState.LOADED
                        )
                    )
                    response.results.let { callback.onResult(it, null, page++) }
                }, { throwable ->
                    networkState.postValue(
                        Event(
                            NetworkState.error(throwable.message)
                        )
                    )
                })
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, AnimeManga>) {
        networkState.postValue(Event(NetworkState.LOADING))
        compositeDisposable.add(
            service.searchAnime(type, searchKey, page, order, sort)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(
                    { response ->
                        networkState.postValue(
                            Event(
                                NetworkState.LOADED
                            )
                        )
                        val nextKey =
                            if (params.key == response.results.size) null else page++
                        response.results.let { callback.onResult(it, nextKey) }
                    }, { throwable ->
                        networkState.postValue(
                            Event(
                                NetworkState.error(throwable.message)
                            )
                        )
                    })
        )

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, AnimeManga>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}