package com.example.anidex.presentation


import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.example.anidex.model.AnimeManga
import com.example.anidex.model.NetworkState
import com.example.anidex.network.APIService

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class AnimeDataSource(
    private val service: APIService,
    private val compositeDisposable: CompositeDisposable,
    private val type: String?,
    private var page: Int,
    private var request: String?,
    private var url: String = "https://api.jikan.moe/v3/top/$type/$page/$request"
) : PageKeyedDataSource<Int, AnimeManga>() {
    var networkState = MutableLiveData<Event<NetworkState>>()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, AnimeManga>

    ) {
        networkState.postValue(Event(NetworkState.LOADING))
        if("null" in url) url = url.replace("/null","")
        if(url.endsWith('/')) url = url.dropLast(1)
        compositeDisposable.add(
            service.getSeries(url)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ anime ->
                    anime.top?.let {
                        callback.onResult(it, null, page)
                        networkState.postValue(Event(NetworkState.LOADED))
                    }
                }, { throwable ->
                    networkState.postValue(Event( NetworkState.error(throwable.message)))
                })
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, AnimeManga>) {
        networkState.postValue(Event(NetworkState.LOADING))
        var url = "https://api.jikan.moe/v3/top/$type/$page/$request"
        if("null" in url) url = url.replace("/null","")
        if(url.endsWith('/')) url = url.dropLast(1)
        compositeDisposable.add(
            service.getSeries(url)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(
                    { anime ->

                        val nextPage =
                            if (params.key == anime.top?.size) null else page++
                        anime.top?.let {
                            callback.onResult(it, nextPage)
                            networkState.postValue(
                                Event(
                                    NetworkState.LOADED
                                )
                            )
                        }
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