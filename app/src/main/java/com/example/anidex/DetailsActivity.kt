package com.example.anidex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.details_layout.*

class DetailsActivity: AppCompatActivity() {
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_layout)
        detail_title.text = intent.getStringExtra("title")
        detail_releasedate.text = intent.getStringExtra("startDate")
        rankdetails.text = String.format(resources.getString(R.string.ranklabel), intent.getStringExtra("rank"))
        scoredetails.text = intent.getStringExtra("score")
        detail_releasedate.text = intent.getStringExtra("fromdate")
        detail_status.text = intent.getStringExtra("status")
        val enddate = intent.getStringExtra("enddate")
        toolbar_details_main.text = intent.getStringExtra("title")
        if(enddate!="Null null, null")
            enddate_detail.text = enddate
        synopsis_view.text = intent.getStringExtra("synopsis")
        englishtitledetail.text = intent.getStringExtra("englishtitle")
        Glide
            .with(this@DetailsActivity)
            .load(intent.getStringExtra("image"))
            .transition(DrawableTransitionOptions())
            .fitCenter()
            .into(detail_image)
        setSupportActionBar(details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        details_toolbar.setNavigationOnClickListener {
            finish()
        }

    }

}