package com.example.anidex.presentation

import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.anidex.R
import com.example.anidex.databinding.CardLayoutBinding
import com.example.anidex.model.AnimeDetail
import com.example.anidex.model.AnimeManga
import kotlin.math.roundToInt

fun <T : RecyclerView.ViewHolder> T.onClick(event: (view: View, position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(it, adapterPosition, itemViewType)
    }
    return this
}

class CardViewHolder(private val cardBinding: CardLayoutBinding) : RecyclerView.ViewHolder(cardBinding.root) {
    fun bind(data: AnimeManga?) {
        Glide
            .with(itemView.context)
            .asBitmap()
            .load(data?.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(BitmapTransitionOptions.withCrossFade())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>,
                    dataSource: DataSource,
                    p4: Boolean
                ): Boolean {
                    val p = Palette.from(resource).generate()
                    val pColorPalette =
                        p.getVibrantColor(ContextCompat.getColor(itemView.context, R.color.bgColor))
                    cardBinding.cardView.setCardBackgroundColor(manipulateColor(pColorPalette, 0.68f))
                    cardBinding.cardepisode.setCardBackgroundColor(
                        manipulateColor(
                            pColorPalette,
                            0.68f
                        )
                    )
                    return false
                }
            })
            .fitCenter()
            .into(cardBinding.thumbnail)
        val epsstring: String
        val episodecount: Int
        if (data?.type != "Manga") {
            episodecount = data?.episodes!!
            epsstring = if (episodecount != 0)
                episodecount.toString() + "eps"
            else "Ongoing"
        } else {
            episodecount = data.volumes
            epsstring = if (episodecount != 0)
                episodecount.toString() + "vols"
            else "Ongoing"
        }
        cardBinding.episodeCt.text = epsstring
        cardBinding.title.text = data.title
        cardBinding.userRating.text = data.score.toString()
    }
    fun bind(data: AnimeDetail?) {
        Glide
            .with(itemView.context)
            .asBitmap()
            .load(data?.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(BitmapTransitionOptions.withCrossFade())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>,
                    dataSource: DataSource,
                    p4: Boolean
                ): Boolean {
                    val p = Palette.from(resource).generate()
                    val pColorPalette =
                        p.getVibrantColor(ContextCompat.getColor(itemView.context, R.color.bgColor))
                    cardBinding.cardView.setCardBackgroundColor(manipulateColor(pColorPalette, 0.68f))
                    cardBinding.cardepisode.setCardBackgroundColor(
                        manipulateColor(
                            pColorPalette,
                            0.68f
                        )
                    )
                    return false
                }
            })
            .fitCenter()
            .into(cardBinding.thumbnail)

        val episodecount: Int = data?.episodes!!
        val epsstring = if (episodecount != 0)
            episodecount.toString() + "eps"
        else "Ongoing"
        cardBinding.episodeCt.text = epsstring
        cardBinding.title.text = data.title
        cardBinding.userRating.text = data.score.toString()
    }

    companion object {
        fun createHolder(parent: ViewGroup): CardViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = CardLayoutBinding.inflate(layoutInflater, parent, false)
            return CardViewHolder(view)
        }
    }

    fun manipulateColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).roundToInt()
        val g = (Color.green(color) * factor).roundToInt()
        val b = (Color.blue(color) * factor).roundToInt()
        return Color.argb(
            a,
            r.coerceAtMost(255),
            g.coerceAtMost(255),
            b.coerceAtMost(255)
        )
    }

}