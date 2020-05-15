package com.example.anidex.ui

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.anidex.model.AnimeManga
import com.example.anidex.model.NetworkState
import com.example.anidex.model.NetworkState.Companion.LOADED

class AnimeAdapter(val itemClickListener: (View, Int, Int) -> Unit) :
    PagedListAdapter<AnimeManga, RecyclerView.ViewHolder>(AniDiffUtil()) {
    private var networkState: NetworkState? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewCVH = CardViewHolder.createHolder(parent)
        viewCVH.onClick(itemClickListener)
        return viewCVH

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is CardViewHolder)
            holder.bind(getItem(position))

    }


    private fun loadedExtraRow(): Boolean {
        return networkState != null && networkState != LOADED

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