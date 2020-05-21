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
    private var request: String?
) : PageKeyedDataSource<Int, AnimeManga>() {
    var networkState = MutableLiveData<Event<NetworkState>>()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, AnimeManga>

    ) {
        networkState.postValue(Event(NetworkState.LOADING))
        compositeDisposable.add(
            service.getSeries(page, type, request)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ anime ->
                    anime.top?.let {
                        callback.onResult(it, null, page++)
                        networkState.postValue(Event(NetworkState.LOADED))
                    }
                }, { throwable ->
                    networkState.postValue(Event( NetworkState.error(throwable.message)))
                })
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, AnimeManga>) {
        networkState.postValue(Event(NetworkState.LOADING))
        compositeDisposable.add(
            service.getSeries(page, type, request)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(
                    { anime ->

                        val nextKey =
                            if (params.key == anime.top?.size) null else page++
                        anime.top?.let {
                            callback.onResult(it, nextKey)
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