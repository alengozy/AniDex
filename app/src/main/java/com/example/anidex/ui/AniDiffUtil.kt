package com.example.anidex.ui

import androidx.recyclerview.widget.DiffUtil
import com.example.anidex.model.AnimeManga

class AniDiffUtil : DiffUtil.ItemCallback<AnimeManga>() {
    override fun areItemsTheSame(oldItem: AnimeManga, newItem: AnimeManga): Boolean {
        return oldItem.malId == newItem.malId
    }

    override fun areContentsTheSame(oldItem: AnimeManga, newItem: AnimeManga): Boolean {
        return oldItem == newItem
    }


}