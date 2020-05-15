package com.example.anidex

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.anidex.model.*
import com.example.anidex.network.APIService
import com.example.anidex.ui.*
import com.google.android.material.navigation.NavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.drawer_layout.*
import java.time.OffsetDateTime
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var viewModel: AnimeViewModel
    private lateinit var animeadapter: AnimeAdapter
    private lateinit var loadingDialog: Dialog
    private var detailNetworkState: MutableLiveData<NetworkState> = MutableLiveData()
    private val service: APIService = APIService.createClient()
    private val compositeDisposable = CompositeDisposable()


    @ExperimentalStdlibApi
    private val itemOnClick: (View, Int, Int) -> Unit = { _, position, _ ->
        val listItem = viewModel.animeList.value?.get(position)
        Toast.makeText(this@MainActivity, listItem?.title.toString(), Toast.LENGTH_SHORT).show()
        initDialog()
        val detailIntent = Intent(this, DetailsActivity::class.java)
        Handler().postDelayed({
            val type = viewModel.type.value
            if (type == "anime")
                fetchAnimeDetails(listItem, detailIntent)
            else if (type == "manga") fetchMangaDetails(listItem, detailIntent)
        }, 300)
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(0, 0)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        viewModel = ViewModelProvider(this).get(AnimeViewModel::class.java)
        initDialog()
        initViews()
        initSwipeRefresh()
        initObservers()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_search) {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            intent.putExtra("type", viewModel.type.value)
            startActivity(intent)
            return false
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_anime_airing) {
            if (viewModel.type.value != "anime") {
                main_toolbar_sub.text = getString(R.string.menuanime)
                viewModel.type.postValue("anime")
            }
            if (viewModel.request.value != "airing") {
                viewModel.request.postValue("airing")
                main_toolbar_text.text = getString(R.string.pref_airing)

            }
        }
        if (id == R.id.nav_anime_upcoming) {
            if (viewModel.type.value != "anime") {
                main_toolbar_sub.text = getString(R.string.menuanime)
                viewModel.type.postValue("anime")
            }
            if (viewModel.request.value != "upcoming") {
                viewModel.request.postValue("upcoming")
                main_toolbar_text.text = getString(R.string.pref_upcoming)
            }

        }
        if (id == R.id.nav_anime_popular) {
            if (viewModel.type.value != "anime") {
                main_toolbar_sub.text = getString(R.string.menuanime)
                viewModel.type.postValue("anime")
            }
            if (viewModel.request.value != "bypopularity") {
                viewModel.request.postValue("bypopularity")
                main_toolbar_text.text = getString(R.string.pref_by_popularity)
            }
        }
        if (id == R.id.nav_anime_highest_rated) {
            if (viewModel.type.value != "anime") {
                main_toolbar_sub.text = getString(R.string.menuanime)
                viewModel.type.postValue("anime")
            }
            if (viewModel.request.value != "") {
                viewModel.request.postValue("")
                main_toolbar_text.text = getString(R.string.pref_highest_rated)
            }
        }
        if (id == R.id.nav_manga_popular) {
            if (viewModel.type.value != "manga") {
                main_toolbar_sub.text = getString(R.string.menumanga)
                viewModel.type.postValue("manga")
            }
            if (viewModel.request.value != "bypopularity") {
                viewModel.request.postValue("bypopularity")
            }
        }
        if (id == R.id.nav_manga_highest_rated) {
            if (viewModel.type.value != "manga") {
                main_toolbar_sub.text = getString(R.string.menumanga)
                viewModel.type.postValue("manga")
            }
            if (viewModel.request.value != "") {
                viewModel.request.postValue("")
            }
        }
        item.isChecked = true
        initObservers()
        drawerLayout.closeDrawers()
        return true
    }


    @ExperimentalStdlibApi
    private fun initViews() {
        animeadapter = AnimeAdapter(itemOnClick)
        rv_anime.apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager =
                if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    GridLayoutManager(this@MainActivity, 2)
                } else {

                    GridLayoutManager(this@MainActivity, 4)
                }
            if (adapter == null) {
                adapter = animeadapter
            }
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    viewModel.animeList.value?.get(2)?.title.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.animeList.observe(
            this@MainActivity,
            Observer<PagedList<AnimeManga>> { animeadapter.submitList(it) })


    }

    private fun initSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    @ExperimentalStdlibApi
    private fun fetchAnimeDetails(listItem: AnimeManga?, intent: Intent) {


        detailNetworkState.postValue(NetworkState.LOADING)
        compositeDisposable.add(service.getAnimeDetail(listItem?.malId, "anime", "")
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


        detailNetworkState.postValue(NetworkState.LOADING)
        compositeDisposable.add(service.getMangaDetail(listItem?.malId, "manga", "")
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
        compositeDisposable.add(service.getCharactersDetail(malId, type, request)
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
        fetchCharacters(listItem?.malId, viewModel.type.value, intent)
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
        fetchCharacters(listItem?.malId, viewModel.type.value, intent)
    }

    private fun onCharacterSuccess(response: Characters?, intent: Intent) {
        intent.putExtra("characters", response)
        detailNetworkState.postValue(NetworkState.LOADED)
        startActivity(intent)
    }


    private fun onError(t: Throwable) {
        detailNetworkState.postValue(NetworkState.error(t.message))
    }

    private fun initObservers() {
        Handler().postDelayed({
            viewModel.nState.observe(this,
                EventObserver { networkState ->
                    swipeRefreshLayout.isRefreshing =
                        networkState.status == NetworkState.LOADING.status
                })
            viewModel.nState.observe(this@MainActivity,
                EventObserver { networkState ->
                    if (networkState.status != NetworkState.LOADING.status && networkState.status != NetworkState.LOADED.status)
                        Toast.makeText(
                            this@MainActivity,
                            "${networkState.message}. Swipe-up to try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    swipeRefreshLayout.isRefreshing = false
                })
            viewModel.nState.observe(
                this@MainActivity,
                EventObserver {
                    animeadapter.setNetworkState(
                        it
                    )
                })
        }, 100)


    }

    private fun initDialog() {
        loadingDialog = Dialog(this@MainActivity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.loadingdialoglayout)
        detailNetworkState.observe(this@MainActivity, Observer {
            if (detailNetworkState.value?.status == NetworkState.LOADING.status)
                loadingDialog.show()
            else
                loadingDialog.dismiss()
        })

    }

}
