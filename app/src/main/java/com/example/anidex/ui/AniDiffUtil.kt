package com.example.anidex.ui

import androidx.recyclerview.widget.DiffUtil
import com.example.anidex.model.Anime

class AniDiffUtil: DiffUtil.ItemCallback<Anime>(){
    override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem.malId == newItem.malId
    }

    override fun areContentsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem == newItem
    }


}