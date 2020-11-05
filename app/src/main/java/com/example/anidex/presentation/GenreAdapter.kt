package com.example.anidex.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.anidex.R
import com.example.anidex.databinding.GenreitemLayoutBinding
import com.example.anidex.model.Genre
import java.util.ArrayList

class GenreAdapter(private val dataSource: ArrayList<Genre>?, private val context: Context) :
    RecyclerView.Adapter<GenreViewHolder>() {
    override fun getItemCount(): Int {
        return dataSource?.size ?: 0

    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = GenreitemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)


    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.genrename.text = dataSource?.get(position)?.name

    }

}

class GenreViewHolder(genreBinding: GenreitemLayoutBinding) : RecyclerView.ViewHolder(genreBinding.root) {
    // Holds the TextView that will add each animal to
    val genrename: TextView = genreBinding.genrecardtext
}
