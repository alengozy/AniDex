package com.example.anidex

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.anidex.model.Character
import com.example.anidex.network.APIService
import kotlinx.android.synthetic.main.details_layout.*


class DetailsActivity: AppCompatActivity() {
    lateinit var synopsis: String
    lateinit var characters: List<Character>
    val service: APIService = APIService.createClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_layout)
        detail_title.text = intent.getStringExtra("title")
        Glide
            .with(this@DetailsActivity)
            .load(intent.getStringExtra("image"))
            .transition(DrawableTransitionOptions())
            .fitCenter()
            .into(detail_image)
        setSupportActionBar(details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //tabLayout.addTab(tabLayout.newTab().setText("Overview"))
        //tabLayout.addTab(tabLayout.newTab().setText("Episodes"))

    }
    fun initJSON(){

    }
}