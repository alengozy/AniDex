package com.example.anidex.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.anidex.R
import com.example.anidex.model.Anime
import kotlinx.android.synthetic.main.card_layout.view.*
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.anidex.MainActivity


class CardViewBinder(val context: Context,val block:(data: Anime)->Unit): FeedItemViewBinder<Anime,CardViewHolder>(
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
        return oldItem == newItem
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
                .asBitmap()
                .load(data.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.ic_launcher)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {

                        return false
                    }
                    override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, p4: Boolean): Boolean {
                        val p = Palette.from(resource).generate()
                        val pColorPalette = p.getMutedColor(ContextCompat.getColor(itemView.context, R.color.bgColor))
                        itemView.card_view.setCardBackgroundColor(manipulateColor(pColorPalette, 0.68f))
                        return false
                    }
                })
                .fitCenter()
                .into(itemView.thumbnail)

            itemView.title.text = data.title
            itemView.user_rating.text = data.score.toString()

        }


    }

    fun manipulateColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.round(Color.red(color) * factor)
        val g = Math.round(Color.green(color) * factor)
        val b = Math.round(Color.blue(color) * factor)
        return Color.argb(
            a,
            Math.min(r, 255),
            Math.min(g, 255),
            Math.min(b, 255)
        )
    }
}