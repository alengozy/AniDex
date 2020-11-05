package com.example.anidex.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.anidex.databinding.CharacterItemBinding
import com.example.anidex.model.Character


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
        val binding = CharacterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding)
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

class CharacterViewHolder(characterBinding: CharacterItemBinding) : RecyclerView.ViewHolder(characterBinding.root) {
    val name: TextView = characterBinding.charName
    val role: TextView = characterBinding.roletext
    val img: ImageView = characterBinding.characterImg
}
