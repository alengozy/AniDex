package com.example.anidex.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.anidex.R
import com.example.anidex.model.Genre
import kotlinx.android.synthetic.main.genreitem_layout.view.*

class GenreAdapter(private val dataSource: List<Genre>, private val context: Context): RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int {
        return dataSource.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.genreitem_layout, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.genrename?.text = dataSource.get(position).name
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val genrename = view.genrecardtext
}
