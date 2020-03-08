package com.example.anidex.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anidex.R
import com.example.anidex.model.Anime
import kotlinx.android.synthetic.main.card_layout.view.*
import kotlinx.android.synthetic.main.nav_header.view.*

class CardViewBinder(val block:(data: Anime)->Unit): FeedItemViewBinder<Anime,CardViewHolder>(
  Anime::class.java){
    override fun createViewHolder(parent: ViewGroup): CardViewHolder {
        return CardViewHolder(
            LayoutInflater.from(parent.context).inflate(getFeedItemType(), parent, false),block)

    }

    override fun bindViewHolder(model: Anime, viewHolder: CardViewHolder) {
        viewHolder.bind(model)
    }

    override fun getFeedItemType() = R.layout.card_layout

    override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem

    override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
        return oldItem.imageUrl == newItem.imageUrl
    }
}

class CardViewHolder(val view: View, val block: (data: Anime)->Unit): RecyclerView.ViewHolder(view){

    fun bind(data: Anime){
        itemView.setOnClickListener{
            block(data)
        }
        itemView.apply{
         Glide
             .with(itemView.context)
             .load(data.imageUrl)
             .centerCrop()
             .into(itemView.imageView)
            itemView.title.text = data.title
            itemView.user_rating.text = data.score.toString()

        }


    }

}