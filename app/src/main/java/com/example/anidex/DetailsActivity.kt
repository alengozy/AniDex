package com.example.anidex

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import kotlinx.android.synthetic.main.details_layout.*
import kotlinx.android.synthetic.main.nav_header.view.*

class DetailsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_layout)
        detail_title.text = intent.getStringExtra("title")
        Glide
            .with(this@DetailsActivity)
            .load(intent.getStringExtra("image"))
            //.transition(BitmapTransitionOptions.withCrossFade())
            .fitCenter()
            .into(detail_image)
    }
}