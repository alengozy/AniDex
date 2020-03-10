package com.example.anidex.ui

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.anidex.R
import com.example.anidex.model.Anime
import com.example.anidex.model.NetworkState
import com.example.anidex.model.NetworkState.Companion.LOADED

class AnimeAdapter: PagedListAdapter<Anime, RecyclerView.ViewHolder>(AniDiffUtil()){
    private var networkState: NetworkState? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CardViewHolder.createHolder(parent)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as CardViewHolder).bind(getItem(position))
    }


    private fun loadedExtraRow(): Boolean{
        return networkState!=null && networkState!=LOADED

    }
    fun setNetworkState(newNetworkState: NetworkState?) {
        if (currentList != null) {
            if (currentList!!.size != 0) {
                val previousState = this.networkState
                val hadExtraRow = loadedExtraRow()
                this.networkState = newNetworkState
                val hasExtraRow = loadedExtraRow()
                if (hadExtraRow != hasExtraRow) {
                    if (hadExtraRow) {
                        notifyDataSetChanged()
                    }
                } else if (hasExtraRow && previousState !== newNetworkState) {
                    notifyItemChanged(itemCount - 2)
                }
            }
        }
    }

}