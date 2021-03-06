package com.example.anidex

import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.FtsOptions
import com.example.anidex.databinding.ActivityMainBinding
import com.example.anidex.model.*
import com.example.anidex.network.APIService
import com.example.anidex.network.AuthServiceConfig
import com.example.anidex.presentation.AnimeAdapter
import com.example.anidex.presentation.AnimeViewModel
import com.example.anidex.presentation.EventObserver
import com.google.android.material.navigation.NavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import java.io.InputStream
import java.time.OffsetDateTime
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var viewModel: AnimeViewModel
    private lateinit var animeadapter: AnimeAdapter
    private lateinit var loadingDialog: Dialog
    private var detailNetworkState: MutableLiveData<NetworkState> = MutableLiveData()
    private val service: APIService = APIService.createJikanClient()
    private val compositeDisposable = CompositeDisposable()
    private var authServiceConfig: AuthServiceConfig = AuthServiceConfig()
    private var typeanime: String = "anime"
    private var typemanga: String = "manga"
    private var filterpop: String = "bypopularity"
    private var filterupcoming: String = "upcoming"
    private var filterairing: String = "airing"
    private var authState: AuthState? = null
    private var url: String = ""


    @ExperimentalStdlibApi
    private val itemOnClick: (View, Int, Int) -> Unit = { _, position, _ ->
        val listItem = viewModel.animeList.value?.get(position)
        Toast.makeText(this@MainActivity, listItem?.title.toString(), Toast.LENGTH_SHORT).show()
        initDialog()
        val detailIntent = Intent(this, DetailsActivity::class.java)
        Handler().postDelayed({
            val type = viewModel.type.value
            if (type == typeanime)
                fetchAnimeDetails(listItem, detailIntent)
            else if (type == typemanga) fetchMangaDetails(listItem, detailIntent)
        }, 300)
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        overridePendingTransition(0, 0)
        toolbar = activityMainBinding.toolbar
        setSupportActionBar(toolbar)
        drawerLayout = activityMainBinding.drawerLayout
        navigationView = activityMainBinding.navView
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
        checkAuthState()
    }

    private fun checkAuthState(){
        authState = readAuthState()
        if(authState == null){
            showLoggedOutElements()
        } else {
            showLoggedInElements()
        }
    }

    private fun showLoggedInElements(){
        val navMenu = navigationView.menu
        navMenu.findItem(R.id.nav_foryou).isVisible = true
        navMenu.findItem(R.id.nav_logout).isVisible = true
        navMenu.findItem(R.id.nav_login).isVisible = false
        invalidateOptionsMenu()
    }
    private fun showLoggedOutElements(){
        val navMenu = navigationView.menu
        navMenu.findItem(R.id.nav_foryou).isVisible = false
        navMenu.findItem(R.id.nav_logout).isVisible = false
        navMenu.findItem(R.id.nav_login).isVisible = true
        invalidateOptionsMenu()
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
        if (id == R.id.nav_login) {
            val authService = AuthorizationService(this)
            val authRequest: AuthorizationRequest = authServiceConfig.getAuthRequest()
            authService.performAuthorizationRequest(
                authRequest,
                PendingIntent.getActivity(
                    this, 0, Intent(
                        this@MainActivity,
                        RecommendationActivity::class.java
                    ), 0
                ),
                PendingIntent.getActivity(
                    this, 0, Intent(
                        this@MainActivity,
                        MainActivity::class.java
                    ), 0
                )
            )
        }
        if (id == R.id.nav_logout) {
            deleteSharedPreferences("auth")
            restartForAuthState()

        }
        if (id == R.id.nav_foryou) {
            if (authState != null) {
                val intent = Intent(this@MainActivity, RecommendationActivity::class.java)
                intent.putExtra("authState", authState!!.jsonSerializeString())
                startActivity(intent)
            }
        }
        if (id == R.id.nav_anime_airing) {
            if (viewModel.type.value != typeanime) {
                    activityMainBinding.mainToolbarSub.text = getString(R.string.menuanime)
                    viewModel.type.postValue(typeanime)
                }
            if (viewModel.request.value != filterairing) {
                    viewModel.request.postValue(filterairing)
                    activityMainBinding.mainToolbarText.text = getString(R.string.pref_airing)

                }
            }
        if (id == R.id.nav_anime_upcoming) {
            if (viewModel.type.value != typeanime) {
                    activityMainBinding.mainToolbarSub.text = getString(R.string.menuanime)
                    viewModel.type.postValue(typeanime)
                }
            if (viewModel.request.value != filterupcoming) {
                    viewModel.request.postValue(filterupcoming)
                    activityMainBinding.mainToolbarText.text = getString(R.string.pref_upcoming)
                }

            }
        if (id == R.id.nav_anime_popular) {
            if (viewModel.type.value != typeanime) {
                    activityMainBinding.mainToolbarSub.text = getString(R.string.menuanime)
                    viewModel.type.postValue(typeanime)
                }
            if (viewModel.request.value != filterpop) {
                    viewModel.request.postValue(filterpop)
                    activityMainBinding.mainToolbarText.text =
                        getString(R.string.pref_by_popularity)
                }
            }
        if (id == R.id.nav_anime_highest_rated) {
            if (viewModel.type.value != typeanime) {
                    activityMainBinding.mainToolbarSub.text = getString(R.string.menuanime)
                    viewModel.type.postValue(typeanime)
                }
            if (viewModel.request.value != "") {
                    viewModel.request.postValue("")
                    activityMainBinding.mainToolbarText.text =
                        getString(R.string.pref_highest_rated)
                }
            }
        if (id == R.id.nav_manga_popular) {
            if (viewModel.type.value != typemanga) {
                    activityMainBinding.mainToolbarSub.text = getString(R.string.menumanga)
                    viewModel.type.postValue(typemanga)
                }
            if (viewModel.request.value != filterpop) {
                    viewModel.request.postValue(filterpop)
                    activityMainBinding.mainToolbarText.text =
                        getString(R.string.pref_by_popularity)
                }
            }
        if (id == R.id.nav_manga_highest_rated) {
            if (viewModel.type.value != typemanga) {
                    activityMainBinding.mainToolbarSub.text = getString(R.string.menumanga)
                    viewModel.type.postValue(typemanga)
                }
            if (viewModel.request.value != "") {
                    viewModel.request.postValue("")
                    activityMainBinding.mainToolbarText.text =
                        getString(R.string.pref_highest_rated)
                }
            }
            item.isChecked = true
            initObservers()
            drawerLayout.closeDrawers()
            return true
        }


    private fun restartForAuthState(){
        finish()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @ExperimentalStdlibApi
    private fun initViews() {
        animeadapter = AnimeAdapter(itemOnClick)
        activityMainBinding.rvAnime.apply {
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
            { animeadapter.submitList(it) })


    }

    private fun initSwipeRefresh() {
        activityMainBinding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
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
        activityMainBinding.swipeRefreshLayout.isRefreshing = false
        Handler().postDelayed({
            viewModel.nState.observe(this,
                EventObserver { networkState ->
                    activityMainBinding.swipeRefreshLayout.isRefreshing =
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
                    activityMainBinding.swipeRefreshLayout.isRefreshing = false
                })
            viewModel.nState.observe(
                this@MainActivity,
                EventObserver {
                    animeadapter.setNetworkState(
                        it
                    )
                })
        }, 2000)


    }

    private fun initDialog() {
        loadingDialog = Dialog(this@MainActivity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.loadingdialoglayout)
        detailNetworkState.observe(this@MainActivity, {
            if (detailNetworkState.value?.status == NetworkState.LOADING.status)
                loadingDialog.show()
            else
                loadingDialog.dismiss()
        })

    }


    @NonNull
    fun readAuthState(): AuthState? {
        val authPrefs = getSharedPreferences("auth", MODE_PRIVATE)
        val data = authPrefs.getString("stateJson", null)
        return if (data != null) {
            AuthState.jsonDeserialize(data)
        } else {
            null
        }
    }

    override fun onResume(){
        super.onResume()
        checkAuthState()
    }


}
