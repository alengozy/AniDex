package com.example.anidex.ui

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.example.anidex.model.AnimeManga
import com.example.anidex.network.APIService
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SearchDataSourceFactory(
    private val service: APIService,
    private val compositeDisposable: CompositeDisposable,
    private val searchKey: String?,
    private val order: String?,
    private val sort: String?,
    private val type: String?
) : DataSource.Factory<Int, AnimeManga>() {
    val mutableLiveData: MutableLiveData<SearchDataSource> = MutableLiveData()

    override fun create(): DataSource<Int, AnimeManga> {
        val searchDataSource = SearchDataSource(service, compositeDisposable, searchKey, type, order, sort, 1)
        mutableLiveData.postValue(searchDataSource)
        return searchDataSource
    }
}