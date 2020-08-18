package com.example.anidex

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.anidex.model.Characters
import com.example.anidex.presentation.CharacterAdapter
import com.example.anidex.presentation.GenreAdapter
import kotlinx.android.synthetic.main.details_layout.*

class DetailsActivity : AppCompatActivity() {
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_layout)
        setSupportActionBar(details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        details_toolbar.setNavigationOnClickListener {
            finish()
        }
        detail_title.text = intent.getStringExtra("title")
        detail_releasedate.text = intent.getStringExtra("startDate")
        rankdetails.text =
            String.format(resources.getString(R.string.ranklabel), intent.getStringExtra("rank"))
        scoredetails.text = intent.getStringExtra("score")
        detail_releasedate.text = intent.getStringExtra("fromdate")
        detail_status.text = intent.getStringExtra("status")
        val enddate = intent.getStringExtra("enddate")
        toolbar_details_main.text = intent.getStringExtra("title")
        if (enddate != "Null null, null")
            enddate_detail.text = enddate
        synopsis_view.text = intent.getStringExtra("synopsis")
        englishtitledetail.text = intent.getStringExtra("englishtitle")
        Glide
            .with(this@DetailsActivity)
            .load(intent.getStringExtra("image"))
            .transition(DrawableTransitionOptions())
            .fitCenter()
            .into(detail_image)

        genrerecycler.adapter =
            GenreAdapter(intent.getParcelableArrayListExtra("genres"), this@DetailsActivity)
        genrerecycler.layoutManager =
            LinearLayoutManager(this@DetailsActivity, LinearLayoutManager.HORIZONTAL, false)
        if (intent.getStringExtra("trailerlink") != "null")
            details_youtube_link.setOnClickListener {
                openYoutubeLink(intent.getStringExtra("trailerlink"))
            } else details_youtube_link.text = "-"
        val characters = intent.getParcelableExtra<Characters>("characters")?.characters
        charactercycler.adapter = CharacterAdapter(characters, this@DetailsActivity)
        charactercycler.layoutManager =
            LinearLayoutManager(this@DetailsActivity, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun openYoutubeLink(youtubeURL: String?) {
        val intentApp = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeURL))
        val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeURL))
        try {
            this.startActivity(intentApp)
        } catch (ex: ActivityNotFoundException) {
            this.startActivity(intentBrowser)
        }

    }


}