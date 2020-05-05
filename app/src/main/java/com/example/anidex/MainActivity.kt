package com.example.anidex

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
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
import com.example.anidex.model.Anime
import com.example.anidex.model.AnimeMangaDetail
import com.example.anidex.model.NetworkState
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

    private lateinit var  toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var animeViewModel: AnimeViewModel
    private lateinit var animeadapter: AnimeAdapter
    private lateinit var loadingDialog: Dialog
    private var detailNetworkState: MutableLiveData<NetworkState> = MutableLiveData()
    private val service: APIService = APIService.createClient()

    @ExperimentalStdlibApi
    private val itemOnClick: (View, Int, Int) -> Unit = { _, position, _ ->
        val listItem = animeViewModel.animeList.value?.get(position)
        Toast.makeText(this@MainActivity, listItem?.title.toString(), Toast.LENGTH_SHORT).show()
        lateinit var intent: Intent
        initDialog()
        fetchDetails(listItem)
    }
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(0, 0)
        swipeRefreshLayout.setOnRefreshListener {
        }
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
        animeViewModel = ViewModelProvider(this).get(AnimeViewModel::class.java)
        initDialog()
        initViews()
        initSwipeRefresh()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    @ExperimentalStdlibApi
    private fun initViews(){
        animeadapter = AnimeAdapter(itemOnClick)
        rv_anime.apply{
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager =
                if(this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    GridLayoutManager(this@MainActivity, 2)
                } else {

                    GridLayoutManager(this@MainActivity, 4)
                }
            if(adapter == null){
                adapter = animeadapter
            }
            setOnClickListener { Toast.makeText(this@MainActivity, animeViewModel.animeList.value?.get(2)?.title.toString(), Toast.LENGTH_SHORT).show()  }
            animeViewModel.animeList.observe(this@MainActivity, Observer<PagedList<Anime>>{animeadapter.submitList(it)})
            animeViewModel.getNetworkState().observe(this@MainActivity, Observer<NetworkState>{animeadapter.setNetworkState(it)})

        }
        animeViewModel.getNetworkState().observe(this@MainActivity, Observer{networkState -> if(networkState?.status != NetworkState.LOADING.status && networkState?.status!=NetworkState.LOADED.status)
            Toast.makeText(this@MainActivity, "${networkState.message}. Swipe-up to try again", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
        })
    }
    private fun initSwipeRefresh(){
        animeViewModel.getNetworkState().observe(this, Observer{networkState ->
                    swipeRefreshLayout.isRefreshing = networkState?.status == NetworkState.LOADING.status

        })

        swipeRefreshLayout.setOnRefreshListener {animeViewModel.refresh()}
    }

    @ExperimentalStdlibApi
    private fun fetchDetails(listItem: Anime?){
        val compositeDisposable = CompositeDisposable()
        detailNetworkState.postValue(NetworkState.LOADING)
        compositeDisposable.add(service.getAnimeDetail(listItem?.malId)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({ response->onSuccess(response, listItem)
            }, { t->onError(t)
            }))
    }

    @ExperimentalStdlibApi
    private fun onSuccess(response: AnimeMangaDetail?, listItem: Anime?){
        intent = Intent(this, DetailsActivity::class.java).apply{
            putExtra("malId", listItem?.malId)
            putExtra("title", listItem?.title.toString())
            putExtra("image", listItem?.imageUrl)
            putExtra("rank", listItem?.rank.toString())
            putExtra("score", listItem?.score.toString())
        }
        val dates = listOf(
            OffsetDateTime.parse(response?.aired?.from),
            if(response?.aired?.to!=null) OffsetDateTime.parse(response.aired.to)else null)
        var month = dates[0]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        var day = dates[0]?.dayOfMonth
        var year = dates[0]?.year
        intent.putExtra("fromdate", String.format(resources.getString(R.string.datestring), month, day, year))
        intent.putExtra( "status", response?.status.toString())
        if(!response?.status.equals("Currently Airing") && !response?.status.equals("Not yet aired")){
            month = dates[1]?.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
            day = dates[1]?.dayOfMonth
            year = dates[1]?.year
            intent.putExtra("enddate", String.format(resources.getString(R.string.datestring), month, day, year))
        }
        intent.putExtra("synopsis", response?.synopsis.toString())
        intent.putExtra("trailerlink", response?.trailer.toString())
        intent.putExtra("episodes", response?.episodes)
        intent.putExtra("englishtitle", response?.englishtitle)
        detailNetworkState.postValue(NetworkState.LOADED)
        startActivity(intent)
    }

    private fun onError(t: Throwable){
        detailNetworkState.postValue(NetworkState.error(t.message))
    }

    private fun initDialog(){
        loadingDialog = Dialog(this@MainActivity)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.loadingdialoglayout)
        detailNetworkState.observe(this@MainActivity, Observer{
            if(detailNetworkState.value?.status == NetworkState.LOADING.status)
                loadingDialog.show()
            else
                loadingDialog.dismiss()
        })

    }

}
