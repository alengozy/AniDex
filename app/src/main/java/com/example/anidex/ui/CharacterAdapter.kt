package com.example.anidex.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.anidex.R
import com.example.anidex.model.Character
import kotlinx.android.synthetic.main.character_item.view.*

class CharacterAdapter(
    private val dataSource: ArrayList<Character>?,
    private val context: Context
) :
    RecyclerView.Adapter<CharacterViewHolder>() {
    override fun getItemCount(): Int {
        return dataSource?.size ?: 0
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        return CharacterViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.character_item,
                parent,
                false
            )
        )
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val data = dataSource?.get(position)
        holder.name.text = data?.name
        holder.role.text = data?.role
        Glide
            .with(context)
            .load(data?.image_url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .fitCenter()
            .into(holder.img)
    }

}

class CharacterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.char_name
    val role: TextView = view.roletext
    val img: ImageView = view.character_img
}
