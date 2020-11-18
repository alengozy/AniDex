package com.example.anidex

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.anidex.databinding.DetailsLayoutBinding
import com.example.anidex.model.Characters
import com.example.anidex.presentation.CharacterAdapter
import com.example.anidex.presentation.GenreAdapter


class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: DetailsLayoutBinding
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.detailsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.detailsToolbar.setNavigationOnClickListener {
            finish()
        }
        initViews()
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

    private fun initViews(){
        binding.detailTitle.text = intent.getStringExtra("title")
        binding.detailReleasedate.text = intent.getStringExtra("startDate")
        binding.rankdetails.text =
            String.format(resources.getString(R.string.ranklabel), intent.getStringExtra("rank"))
        binding.scoredetails.text = intent.getStringExtra("score")
        binding.detailReleasedate.text = intent.getStringExtra("fromdate")
        binding.detailStatus.text = intent.getStringExtra("status")
        val enddate = intent.getStringExtra("enddate")
        binding.toolbarDetailsMain.text = intent.getStringExtra("title")
        if (enddate != "Null null, null")
            binding.enddateDetail.text = enddate
        binding.synopsisView.text = intent.getStringExtra("synopsis")
        binding.englishtitledetail.text = intent.getStringExtra("englishtitle")
        Glide
            .with(this@DetailsActivity)
            .load(intent.getStringExtra("image"))
            .transition(DrawableTransitionOptions())
            .fitCenter()
            .into(binding.detailImage)

        binding.genrerecycler.adapter =
            GenreAdapter(intent.getParcelableArrayListExtra("genres"), this@DetailsActivity)
        binding.genrerecycler.layoutManager =
            LinearLayoutManager(this@DetailsActivity, LinearLayoutManager.HORIZONTAL, false)
        if (intent.getStringExtra("trailerlink") != "null")
            binding.detailsYoutubeLink.setOnClickListener {
                openYoutubeLink(intent.getStringExtra("trailerlink"))
            } else binding.detailsYoutubeLink.text = "-"
        val characters = intent.getParcelableExtra<Characters>("characters")?.characters
        binding.charactercycler.adapter = CharacterAdapter(characters, this@DetailsActivity)
        binding.charactercycler.layoutManager =
            LinearLayoutManager(this@DetailsActivity, LinearLayoutManager.HORIZONTAL, false)

    }


}