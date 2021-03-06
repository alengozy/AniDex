package com.example.anidex

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.anidex.databinding.SearchActivityLayoutBinding
import com.example.anidex.presentation.AnimeAdapter
import com.example.anidex.presentation.SearchViewModel
import com.example.anidex.model.*
import com.example.anidex.network.APIService
import com.example.anidex.presentation.EventObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.OffsetDateTime
import java.util.*


class SearchActivity : AppCompatActivity() {
    @ExperimentalStdlibApi
    private val itemOnClick: (View, Int, Int) -> Unit = { _, position, _ ->
        val listItem = viewModel.animeList.value?.get(position)
        Toast.makeText(this@SearchActivity, listItem?.title.toString(), Toast.LENGTH_SHORT).show()
        initDialog()
        val detailIntent = Intent(this, DetailsActivity::class.java)
        Handler().postDelayed({
            val type = viewModel.type.value
            if (type == "anime")
                fetchAnimeDetails(listItem, detailIntent)
            else if (type == "manga") fetchMangaDetails(listItem, detailIntent)
        }, 300)
    }
    private lateinit var binding: SearchActivityLayoutBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var searchView: SearchView
    private lateinit var animeadapter: AnimeAdapter
    private lateinit var loadingDialog: Dialog
    private val detailNetworkState: MutableLiveData<NetworkState> = MutableLiveData()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val service = APIService.createJikanClient()
    private lateinit var type: String
    private lateinit var url: String

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SearchActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.searchActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.searchActivityToolbar.setNavigationOnClickListener {
            finish()
        }

        type = intent.getStringExtra("type")!!
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        viewModel.type.postValue(type)
        initViews()
        initSwipeRefresh()
        initDialog()
        initObservers()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_activity_toolbar, menu)
        searchView = menu?.findItem(R.id.searchView_toolbar)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                //search_swipeRefreshLayout.isRefreshing = true
                searchView.clearFocus()
                viewModel.searchKey.postValue(query)
                initObservers()
                return false
            }

        })

        return true
    }

    override fun onResume() {
        super.onResume();
        binding.searchActivityToolbar.requestFocus();

    }

    @ExperimentalStdlibApi
    fun initViews() {

        ArrayAdapter.createFromResource(
            this,
            R.array.types_array,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.searchTypeSpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(this, R.array.orderby_array, R.layout.spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.searchOrderbySpinner.adapter = adapter
            }
        if(type=="anime")
            binding.searchTypeSpinner.setSelection(0)
        else binding.searchTypeSpinner.setSelection(1)
        binding.searchTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                    //search_swipeRefreshLayout.isRefreshing = true
                    type = binding.searchTypeSpinner.selectedItem.toString().toLowerCase(Locale.ROOT)
                    viewModel.type.postValue(type)
                    initObservers()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        binding.searchOrderbySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if(position>0){
                //search_swipeRefreshLayout.isRefreshing = true
                val order = binding.searchOrderbySpinner.selectedItem.toString().toLowerCase(Locale.ROOT)
                viewModel.order.postValue(order)
                initObservers()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        animeadapter = AnimeAdapter(itemOnClick)
        binding.rvAnimeSearch.apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager =
                if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    GridLayoutManager(this@SearchActivity, 2)
                } else {

                    GridLayoutManager(this@SearchActivity, 4)
                }
            if (adapter == null) {
                adapter = animeadapter
            }
            setOnClickListener {
                Toast.makeText(
                    this@SearchActivity,
                    viewModel.animeList.value?.get(2)?.title.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.animeList.observe(
            this@SearchActivity,
            Observer<PagedList<AnimeManga>> { animeadapter.submitList(it) })


    }

    private fun initSwipeRefresh() {
        binding.searchSwipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun initObservers() {
        binding.searchSwipeRefreshLayout.isRefreshing = false
        Handler().postDelayed({
            viewModel.nState.observe(this,
                EventObserver { networkState ->
                    binding.searchSwipeRefreshLayout.isRefreshing =
                        networkState.status == NetworkState.LOADING.status
                })
            viewModel.nState.observe(this@SearchActivity,
                EventObserver { networkState ->
                    if (networkState.status != NetworkState.LOADING.status && networkState.status != NetworkState.LOADED.status)
                        Toast.makeText(
                            this@SearchActivity,
                            "${networkState.message}. Swipe-up to try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    binding.searchSwipeRefreshLayout.isRefreshing = false
                })
            viewModel.nState.observe(
                this@SearchActivity,
                EventObserver {
                    animeadapter.setNetworkState(
                        it
                    )
                })
        }, 100)


    }

    @ExperimentalStdlibApi
    private fun fetchAnimeDetails(listItem: AnimeManga?, intent: Intent) {

        val id = listItem?.malId
        url = "https://api.jikan.moe/v3/anime/$id"
        detailNetworkState.postValue(NetworkState.LOADING)
        compositeDisposable.add(
            service.getAnimeDetail(url)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ response ->
                    onAnimeDetailsSuccess(response, listItem, intent)
                }, { t ->
                    onError(t)
                })
        )
    }

    @ExperimentalStdlibApi
    private fun fetchMangaDetails(listItem: AnimeManga?, intent: Intent) {

        val id = listItem?.malId
        url = "https://api.jikan.moe/v3/manga/$id"
        detailNetworkState.postValue(NetworkState.LOADING)
        compositeDisposable.add(
            service.getMangaDetail(url)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ response ->
                    onMangaDetailsSuccess(response, listItem, intent)
                }, { t ->
                    onError(t)
                })
        )
    }

    private fun fetchCharacters(malId: Int?, type: String?, intent: Intent) {
        val request: String = if (type == "anime")
            "characters_staff"
        else "characters"
        url = "https://api.jikan.moe/v3/$type/$malId/$request"
        compositeDisposable.add(
            service.getCharactersDetail(url)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ response ->
                    onCharacterSuccess(response, intent)
                }, { t ->
                    onError(t)
                })
        )
    }


    @ExperimentalStdlibApi
    private fun onAnimeDetailsSuccess(
        response: AnimeDetail?,
        listItem: AnimeManga?,
        intent: Intent
    ) {

        intent.putExtra("malId", listItem?.malId)
        intent.putExtra("title", listItem?.title.toString())
        intent.putExtra("image", listItem?.imageUrl)
        intent.putExtra("score", listItem?.score.toString())

        val dates = listOf(
            OffsetDateTime.parse(response?.aired?.from),
            if (response?.aired?.to != null) OffsetDateTime.parse(response.aired.to) else null
        )
        var month = dates[0]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        var day = dates[0]?.dayOfMonth
        var year = dates[0]?.year
        intent.putExtra(
            "fromdate",
            String.format(resources.getString(R.string.datestring), month, day, year)
        )
        intent.putExtra("status", response?.status.toString())
        if (!response?.status.equals("Currently Airing") && !response?.status.equals("Not yet aired")) {
            month = dates[1]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
            day = dates[1]?.dayOfMonth
            year = dates[1]?.year
            intent.putExtra(
                "enddate",
                String.format(resources.getString(R.string.datestring), month, day, year)
            )
        }
        intent.putExtra("synopsis", response?.synopsis.toString())
        intent.putExtra("trailerlink", response?.trailer.toString())
        intent.putExtra("episodes", response?.episodes)
        intent.putExtra("englishtitle", response?.englishtitle)
        intent.putExtra("genres", response?.genres)
        intent.putExtra("rank", response?.rank.toString())
        fetchCharacters(listItem?.malId, type, intent)
    }

    @ExperimentalStdlibApi
    private fun onMangaDetailsSuccess(
        response: MangaDetail?,
        listItem: AnimeManga?,
        intent: Intent
    ) {

        intent.putExtra("malId", listItem?.malId)
        intent.putExtra("title", listItem?.title.toString())
        intent.putExtra("image", listItem?.imageUrl)
        intent.putExtra("score", listItem?.score.toString())

        val dates = listOf(
            OffsetDateTime.parse(response?.aired?.from),
            if (response?.aired?.to != null) OffsetDateTime.parse(response.aired.to) else null
        )
        var month = dates[0]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        var day = dates[0]?.dayOfMonth
        var year = dates[0]?.year
        intent.putExtra(
            "fromdate",
            String.format(resources.getString(R.string.datestring), month, day, year)
        )
        intent.putExtra("status", response?.status.toString())
        if (!response?.status.equals("Currently Airing") && !response?.status.equals("Not yet aired")) {
            month = dates[1]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
            day = dates[1]?.dayOfMonth
            year = dates[1]?.year
            intent.putExtra(
                "enddate",
                String.format(resources.getString(R.string.datestring), month, day, year)
            )
        }
        intent.putExtra("synopsis", response?.synopsis.toString())
        intent.putExtra("trailerlink", response?.trailer.toString())
        intent.putExtra("episodes", response?.chapters)
        intent.putExtra("englishtitle", response?.englishtitle)
        intent.putExtra("genres", response?.genres)
        intent.putExtra("rank", response?.rank.toString())
        fetchCharacters(listItem?.malId, type, intent)
    }

    private fun onCharacterSuccess(response: Characters?, intent: Intent) {
        intent.putExtra("characters", response)
        detailNetworkState.postValue(NetworkState.LOADED)
        startActivity(intent)
    }


    private fun onError(t: Throwable) {
        detailNetworkState.postValue(NetworkState.error(t.message))
    }

    private fun initDialog() {

        loadingDialog = Dialog(this@SearchActivity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.loadingdialoglayout)
        detailNetworkState.observe(this@SearchActivity, Observer {
            if (detailNetworkState.value?.status == NetworkState.LOADING.status)
                loadingDialog.show()
            else
                loadingDialog.dismiss()
        })

    }
}



