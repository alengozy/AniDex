package com.example.anidex.ui


import android.net.Network
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.example.anidex.model.Anime
import com.example.anidex.model.NetworkState
import com.example.anidex.network.APIService

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers




class AnimeDataSource(private val service: APIService, private val compositeDisposable: CompositeDisposable): PageKeyedDataSource<Int, Anime>(){
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Anime>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        compositeDisposable.add(
            service.getSeries(1)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({

                        anime ->
                    initialLoad.postValue(NetworkState.LOADED)
                    networkState.postValue(NetworkState.LOADED)
                    anime.top?.let { callback.onResult(it, null, 2) }
                }, {
                    throwable ->
                    networkState.postValue(NetworkState.error(throwable.message))
                }))
    }
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Anime>) {
        networkState.postValue(NetworkState.LOADING)
        compositeDisposable.add(
            service.getSeries(params.key)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(
                    {
                            anime ->
                        networkState.postValue(NetworkState.LOADED)

                        val nextKey =
                            if (params.key == anime.top?.size) null else params.key + 1
                        anime.top?.let { callback.onResult(it, nextKey) }
                    }, {
                            throwable ->
                        networkState.postValue(NetworkState.error(throwable.message))
                    }))

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Anime>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}