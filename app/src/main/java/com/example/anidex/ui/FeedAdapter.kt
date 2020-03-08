package com.example.anidex.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FeedAdapter(
    private val viewBinders: Map<FeedItemClass, FeedItemBinder>
) : ListAdapter<Any, RecyclerView.ViewHolder>(GenericDiffUtil(viewBinders)) {

    private val viewTypeToBinders = viewBinders.mapKeys { it.value.getFeedItemType() }

    private fun getViewBinder(viewType: Int): FeedItemBinder = viewTypeToBinders.getValue(viewType)

    override fun getItemViewType(position: Int): Int =
        viewBinders.getValue(super.getItem(position).javaClass).getFeedItemType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getViewBinder(viewType).createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return getViewBinder(getItemViewType(position)).bindViewHolder(getItem(position), holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        getViewBinder(holder.itemViewType).onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        getViewBinder(holder.itemViewType).onViewDetachedFromWindow(holder)
        super.onViewDetachedFromWindow(holder)
    }
}